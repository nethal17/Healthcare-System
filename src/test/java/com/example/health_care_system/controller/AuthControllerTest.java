package com.example.health_care_system.controller;

import com.example.health_care_system.dto.LoginRequest;
import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.exception.AuthenticationException;
import com.example.health_care_system.exception.DuplicateResourceException;
import com.example.health_care_system.exception.ValidationException;
import com.example.health_care_system.service.AuthenticationService;
import com.example.health_care_system.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    UserService userService;
    @Mock
    AuthenticationService authenticationService;
    @Mock
    BindingResult bindingResult;
    @Mock
    RedirectAttributes redirectAttributes;
    @Mock
    Model model;
    @Mock
    HttpSession session;

    AuthController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AuthController(userService, authenticationService);
    }

    @Test
    void showRegisterPage_populatesModel() {
        String view = controller.showRegisterPage(model);
        assertEquals("register", view);
        verify(model).addAttribute(eq("registerRequest"), any(RegisterRequest.class));
    }

    @Test
    void register_success_redirectsToLogin() {
        RegisterRequest req = new RegisterRequest();
        when(bindingResult.hasErrors()).thenReturn(false);
        // registerPatient returns a UserDTO; provide a simple stub value to avoid executing real logic
        when(userService.registerPatient(req)).thenReturn(null);

        String view = controller.register(req, bindingResult, redirectAttributes);
        assertEquals("redirect:/login", view);
        verify(userService).registerPatient(req);
    }

    @Test
    void register_duplicate_showsRegisterRedirect() {
        RegisterRequest req = new RegisterRequest();
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new DuplicateResourceException("dup")).when(userService).registerPatient(req);

        String view = controller.register(req, bindingResult, redirectAttributes);
        assertEquals("redirect:/register", view);
    }

    @Test
    void login_validationErrors_returnsLogin() {
        LoginRequest req = new LoginRequest();
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.login(req, bindingResult, session, redirectAttributes);
        assertEquals("login", view);
    }

    @Test
    void login_authFailure_redirectsLogin() {
        LoginRequest req = new LoginRequest(); req.setEmail("x@ex.com");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authenticationService.authenticate(req)).thenThrow(new AuthenticationException("bad"));

        String view = controller.login(req, bindingResult, session, redirectAttributes);
        assertEquals("redirect:/login", view);
    }

    @Test
    void logout_invalidatesSessionAndRedirects() {
        String view = controller.logout(session, redirectAttributes);
        assertEquals("redirect:/login", view);
        verify(session).invalidate();
    }
}
