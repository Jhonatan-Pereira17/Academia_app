package com.academiaapp.backend.photo;

import com.academiaapp.backend.auth.JwtService;
import com.academiaapp.backend.personal.Personal;
import com.academiaapp.backend.personal.PersonalRepository;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentRepository;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProgressPhotoAccessTest {

    private static final byte[] JPEG_BYTES = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x01, 0x02, 0x03 };
    private static final long MISSING_ID = 999_999_999L;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PersonalRepository personalRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ProgressPhotoRepository photoRepository;
    @Autowired
    private PhotoStorage photoStorage;
    @Autowired
    private ProgressPhotoService photoService;

    private Personal ownerPersonal;
    private Personal otherPersonal;
    private Student owner;
    private Student otherStudent;
    private User admin;
    private ProgressPhoto photo;

    @BeforeEach
    void setUp() {
        assertTrue(datasourceUrl.endsWith("/academia_test"),
                "Os testes só podem rodar no banco academia_test, mas a URL é " + datasourceUrl);

        ownerPersonal = newPersonal("personal-dono");
        otherPersonal = newPersonal("personal-outro");
        owner = newStudent("aluno-dono", ownerPersonal);
        otherStudent = newStudent("aluno-outro", otherPersonal);
        admin = newUser("admin", Role.ADMIN);
        photo = newPhoto(owner);
    }

    // ---------- O Personal: a regra crítica ----------

    @Test
    void personalDoAlunoNaoAcessaNenhumaRotaDeFotos() {
        User personal = ownerPersonal.getUser();
        String base = "/progress-photos/" + photo.getId();

        assertAll(
                () -> getAs(personal, "/progress-photos").andExpect(status().isForbidden()),
                () -> getAs(personal, "/progress-photos?studentId=" + owner.getId()).andExpect(status().isForbidden()),
                () -> getAs(personal, base).andExpect(status().isForbidden()),
                () -> getAs(personal, base + "/file").andExpect(status().isForbidden()),
                () -> deleteAs(personal, base).andExpect(status().isForbidden()),
                () -> uploadAs(personal).andExpect(status().isForbidden()));
    }

    @Test
    void personalNaoDescobreSeUmaFotoExiste() {
        User personal = ownerPersonal.getUser();

        assertAll(
                () -> getAs(personal, "/progress-photos/" + MISSING_ID).andExpect(status().isForbidden()),
                () -> getAs(personal, "/progress-photos/" + MISSING_ID + "/file").andExpect(status().isForbidden()),
                () -> deleteAs(personal, "/progress-photos/" + MISSING_ID).andExpect(status().isForbidden()));
    }

    @Test
    void outroPersonalTambemRecebeAcessoNegado() {
        User personal = otherPersonal.getUser();
        String base = "/progress-photos/" + photo.getId();

        assertAll(
                () -> getAs(personal, "/progress-photos?studentId=" + owner.getId()).andExpect(status().isForbidden()),
                () -> getAs(personal, base).andExpect(status().isForbidden()),
                () -> getAs(personal, base + "/file").andExpect(status().isForbidden()),
                () -> deleteAs(personal, base).andExpect(status().isForbidden()));
    }

    @Test
    void servicoNegaAoPersonalMesmoSemPassarPeloController() {
        String email = ownerPersonal.getUser().getEmail();
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", JPEG_BYTES);

        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> photoService.list(email, owner.getId())),
                () -> assertThrows(AccessDeniedException.class, () -> photoService.findById(email, photo.getId())),
                () -> assertThrows(AccessDeniedException.class, () -> photoService.loadFile(email, photo.getId())),
                () -> assertThrows(AccessDeniedException.class, () -> photoService.delete(email, photo.getId())),
                () -> assertThrows(AccessDeniedException.class, () -> photoService.findById(email, MISSING_ID)),
                () -> assertThrows(AccessDeniedException.class,
                        () -> photoService.upload(email, file, LocalDate.now(), null, null, null)));
    }

    // ---------- O aluno ----------

    @Test
    void alunoDonoVeBaixaEApagaAsProprias() throws Exception {
        User student = owner.getUser();
        String base = "/progress-photos/" + photo.getId();

        getAs(student, "/progress-photos")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(photo.getId().intValue()))
                .andExpect(jsonPath("$[0].storageKey").doesNotExist());

        getAs(student, base + "/file")
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(JPEG_BYTES))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(result -> assertTrue(
                        result.getResponse().getHeader("Cache-Control").contains("no-store")));

        deleteAs(student, base).andExpect(status().isNoContent());
        getAs(student, base).andExpect(status().isNotFound());
    }

    @Test
    void alunoNaoAcessaFotoDeOutroAluno() {
        User stranger = otherStudent.getUser();
        String base = "/progress-photos/" + photo.getId();

        assertAll(
                () -> getAs(stranger, base).andExpect(status().isNotFound()),
                () -> getAs(stranger, base + "/file").andExpect(status().isNotFound()),
                () -> deleteAs(stranger, base).andExpect(status().isNotFound()),
                () -> getAs(stranger, "/progress-photos?studentId=" + owner.getId()).andExpect(status().isForbidden()));
    }

    // ---------- O Admin ----------

    @Test
    void adminVeMasNaoApagaNemEnvia() {
        String base = "/progress-photos/" + photo.getId();

        assertAll(
                () -> getAs(admin, "/progress-photos?studentId=" + owner.getId())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(1)),
                () -> getAs(admin, "/progress-photos").andExpect(status().isBadRequest()),
                () -> getAs(admin, base).andExpect(status().isOk()),
                () -> getAs(admin, base + "/file").andExpect(status().isOk()),
                () -> deleteAs(admin, base).andExpect(status().isForbidden()),
                () -> uploadAs(admin).andExpect(status().isForbidden()));
    }

    // ---------- Sem login ----------

    @Test
    void semTokenRecebe401EmTodasAsRotas() {
        String base = "/progress-photos/" + photo.getId();

        assertAll(
                () -> mockMvc.perform(get("/progress-photos")).andExpect(status().isUnauthorized()),
                () -> mockMvc.perform(get(base)).andExpect(status().isUnauthorized()),
                () -> mockMvc.perform(get(base + "/file")).andExpect(status().isUnauthorized()),
                () -> mockMvc.perform(delete(base)).andExpect(status().isUnauthorized()));
    }

    // ---------- Ferramentas do teste ----------

    private ResultActions getAs(User user, String path) throws Exception {
        return mockMvc.perform(get(path).header("Authorization", bearer(user)));
    }

    private ResultActions deleteAs(User user, String path) throws Exception {
        return mockMvc.perform(delete(path).header("Authorization", bearer(user)));
    }

    private ResultActions uploadAs(User user) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", JPEG_BYTES);
        return mockMvc.perform(multipart("/progress-photos")
                .file(file)
                .param("takenOn", LocalDate.now().toString())
                .header("Authorization", bearer(user)));
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private User newUser(String label, Role role) {
        User user = new User();
        user.setEmail(label + "-" + UUID.randomUUID() + "@teste.local");
        user.setPasswordHash("{noop}senha-nao-usada");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Personal newPersonal(String label) {
        Personal personal = new Personal();
        personal.setUser(newUser(label, Role.PERSONAL));
        personal.setName(label);
        return personalRepository.save(personal);
    }

    private Student newStudent(String label, Personal personal) {
        Student student = new Student();
        student.setUser(newUser(label, Role.STUDENT));
        student.setName(label);
        student.setPersonal(personal);
        return studentRepository.save(student);
    }

    private ProgressPhoto newPhoto(Student student) {
        ProgressPhoto progressPhoto = new ProgressPhoto();
        progressPhoto.setStudent(student);
        progressPhoto.setTakenOn(LocalDate.now());
        progressPhoto.setAngle(PhotoAngle.FRONT);
        progressPhoto.setStorageKey(UUID.randomUUID().toString());
        progressPhoto.setContentType("image/jpeg");
        progressPhoto.setSizeBytes((long) JPEG_BYTES.length);
        photoRepository.save(progressPhoto);
        photoStorage.save(progressPhoto.getStorageKey(), JPEG_BYTES);
        return progressPhoto;
    }
}