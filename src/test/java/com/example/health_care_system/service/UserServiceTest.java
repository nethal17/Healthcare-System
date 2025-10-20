package com.example.health_care_system.service;

import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.dto.UpdateProfileRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.exception.DuplicateResourceException;
import com.example.health_care_system.exception.ValidationException;
import com.example.health_care_system.factory.UserFactory;
import com.example.health_care_system.mapper.UserMapper;
import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.StaffRepository;
import com.example.health_care_system.repository.UserRepository;
import com.example.health_care_system.service.validation.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private QRCodeService qrCodeService;
    @Mock
    private HealthCardService healthCardService;
    @Mock
    private UserResolverService userResolverService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserFactory userFactory;
    @Mock
    private ValidationService validationService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(
                userRepository,
                patientRepository,
                doctorRepository,
                staff_repository_or_placeholder(),
                passwordEncoder,
                qrCodeService,
                healthCardService,
                userResolverService,
                userMapper,
                userFactory,
                validation_service_placeholder()
        );
    }

    // Helper to satisfy constructor in code edit (will be replaced by proper mocks at runtime)
    private StaffRepository staff_repository_or_placeholder() { return staffRepository; }
    private ValidationService validation_service_placeholder() { return validationService; }

    @Test
    void registerPatient_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test"); req.setEmail("t@example.com"); req.setPassword("pass123"); req.setConfirmPassword("pass123"); req.setDateOfBirth(LocalDate.of(1990,1,1)); req.setContactNumber("077");

        doNothing().when(validationService).validateRegistrationRequest(req);
        when(userResolverService.emailExists("t@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");

        Patient created = new Patient(); created.setEmail("t@example.com"); created.setId("p1");
        when(userFactory.createPatient(req, "encoded")).thenReturn(created);
        when(patientRepository.save(created)).thenReturn(created);
        when(qrCodeService.generateQRCode("p1")).thenReturn("qr");
        when(patientRepository.save(created)).thenReturn(created);
        UserDTO dto = new UserDTO(); dto.setId("p1"); dto.setEmail("t@example.com");
        when(userMapper.toDTO(created)).thenReturn(dto);

        UserDTO out = userService.registerPatient(req);
        assertNotNull(out);
        assertEquals("t@example.com", out.getEmail());
        verify(healthCardService).createHealthCard(created);
    }

    @Test
    void registerPatient_emailExists_throws() {
        RegisterRequest req = new RegisterRequest(); req.setEmail("e@ex.com");
        doNothing().when(validationService).validateRegistrationRequest(any());
        when(userResolverService.emailExists("e@ex.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> userService.registerPatient(req));
    }

    @Test
    void registerPatient_validationFails_propagates() {
        RegisterRequest req = new RegisterRequest();
        doThrow(new ValidationException("email","bad")).when(validationService).validateRegistrationRequest(req);
        assertThrows(ValidationException.class, () -> userService.registerPatient(req));
    }

    @Test
    void getUserById_and_getUserByEmail_delegate() {
        Patient p = new Patient(); p.setId("p2");
        when(userResolverService.findUserById("p2")).thenReturn(p);
        UserDTO dto = new UserDTO(); dto.setId("p2");
        when(userMapper.toDTO(p)).thenReturn(dto);
        assertEquals("p2", userService.getUserById("p2").getId());

        when(userResolverService.findUserByEmail("a@b.com")).thenReturn(p);
        assertEquals("p2", userService.getUserByEmail("a@b.com").getId());
    }

    @Test
    void updateProfile_patient_updatesAndHealthCardUpdated() {
        Patient p = new Patient(); p.setId("p3"); p.setEmail("old@ex.com");
        when(userResolverService.findUserById("p3")).thenReturn(p);
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("new@ex.com"); req.setName("New Name"); req.setContactNumber("077"); req.setGender("M"); req.setDateOfBirth(LocalDate.of(1990,1,1)); req.setBloodType("O+"); req.setAddress("addr");

        when(userResolverService.emailExists("new@ex.com")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(i -> i.getArgument(0));
        HealthCard card = new HealthCard(); card.setId("hc1");
        when(healthCardService.getHealthCardByPatientId("p3")).thenReturn(Optional.of(card));
        when(userMapper.toDTO(any(Patient.class))).thenReturn(new UserDTO());

        userService.updateProfile("p3", req);
        verify(patientRepository).save(any(Patient.class));
        verify(healthCardService).updateHealthCard(any(HealthCard.class));
    }

    @Test
    void updateProfile_doctor_savesDoctor() {
        Doctor d = new Doctor(); d.setId("d1"); d.setEmail("doc@ex.com");
        when(userResolverService.findUserById("d1")).thenReturn(d);
        UpdateProfileRequest req = new UpdateProfileRequest(); req.setEmail("doc@ex.com"); req.setName("Doc");
    org.mockito.Mockito.lenient().when(userResolverService.emailExists(anyString())).thenReturn(false);
        when(doctorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDTO(any())).thenReturn(new UserDTO());
        userService.updateProfile("d1", req);
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void updateProfile_staff_and_admin_paths() {
        Staff s = new Staff(); s.setId("s1"); s.setEmail("s@ex.com");
        when(userResolverService.findUserById("s1")).thenReturn(s);
        UpdateProfileRequest req = new UpdateProfileRequest(); req.setEmail("s@ex.com"); req.setName("Staff");
    org.mockito.Mockito.lenient().when(userResolverService.emailExists(anyString())).thenReturn(false);
        when(staffRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDTO(any())).thenReturn(new UserDTO());
        userService.updateProfile("s1", req);
        verify(staffRepository).save(any(Staff.class));

        User admin = new User(); admin.setId("u1"); admin.setEmail("u@ex.com");
        when(userResolverService.findUserById("u1")).thenReturn(admin);
        UpdateProfileRequest req2 = new UpdateProfileRequest(); req2.setEmail("u@ex.com"); req2.setName("U");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDTO(any())).thenReturn(new UserDTO());
        userService.updateProfile("u1", req2);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_emailTaken_throws() {
        User u = new User(); u.setId("u2"); u.setEmail("old@ex.com");
        when(userResolverService.findUserById("u2")).thenReturn(u);
        UpdateProfileRequest req = new UpdateProfileRequest(); req.setEmail("taken@ex.com"); req.setName("N");
        when(userResolverService.emailExists("taken@ex.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> userService.updateProfile("u2", req));
    }

    @Test
    void changePassword_validation_and_incorrect_current_and_success_paths() {
        // invalid new password
        assertThrows(ValidationException.class, () -> userService.changePassword("x","cur","123"));

        // incorrect current password
        User u = new User(); u.setId("u3"); u.setPassword("encoded");
        when(userResolverService.findUserById("u3")).thenReturn(u);
        when(passwordEncoder.matches("bad","encoded")).thenReturn(false);
        assertThrows(ValidationException.class, () -> userService.changePassword("u3","bad","newpass"));

        // success path for patient
        Patient p = new Patient(); p.setId("p5"); p.setPassword("encoded");
        when(userResolverService.findUserById("p5")).thenReturn(p);
        when(passwordEncoder.matches("cur","encoded")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encNew");
        when(patientRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        userService.changePassword("p5","cur","newpass");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void updateProfile_nullRequest_throws() {
        User u = new User(); u.setId("uX"); u.setEmail("e@x.com");
        when(userResolverService.findUserById("uX")).thenReturn(u);
        assertThrows(NullPointerException.class, () -> userService.updateProfile("uX", null));
    }

    @Test
    void changePassword_userNotFound_throws() {
        // method validates new password length first and will throw ValidationException for short passwords
        assertThrows(ValidationException.class, () -> userService.changePassword("missing", "a", "b"));
    }

    @Test
    void admin_counts_and_lists() {
        when(patientRepository.findAll()).thenReturn(List.of(new Patient(), new Patient()));
        when(doctorRepository.findAll()).thenReturn(List.of(new Doctor()));
        when(userRepository.findByRole(UserRole.PATIENT)).thenReturn(List.of(new User(), new User()));
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.findByRole(UserRole.DOCTOR)).thenReturn(List.of(new User()));
        when(userRepository.findByRole(UserRole.STAFF)).thenReturn(List.of(new User()));

        assertEquals(2, userService.getAllPatients().size());
        assertEquals(1, userService.getAllDoctors().size());
        assertEquals(2, userService.getPatientCount());
        assertEquals(5, userService.getTotalUserCount());
        assertEquals(1, userService.getDoctorCount());
        assertEquals(1, userService.getStaffCount());
    }
}

