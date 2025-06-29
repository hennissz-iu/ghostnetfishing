package de.ghostnet.ghostnetfishing.controller;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.service.GhostNetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GhostNetControllerTest {

    private GhostNetService   service;
    private RedirectAttributes redirect;
    private GhostNetController controller;

    @BeforeEach
    void setUp() {
        service    = mock(GhostNetService.class);
        redirect   = mock(RedirectAttributes.class);
        controller = new GhostNetController(service);
    }

    @Test
    void showFormGibtTemplateZurueck() {
        String view = controller.showForm(new ExtendedModelMap());
        assertEquals("netze-erfassen", view);
    }

    @Test
    void saveNetDelegiertAnService() {
        GhostNet net = new GhostNet();
        controller.saveNet(net, redirect);
        verify(service).report(net);
    }

    @Test
    void listDelegiertAnService() {
        controller.list(new ExtendedModelMap());
        verify(service).findAll();
    }
}
