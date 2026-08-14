package com.club;

import com.club.common.BusinessException;
import com.club.controller.SysDictTypeController;
import com.club.security.JwtAuthenticationFilter;
import com.club.security.JwtUtils;
import com.club.security.SecurityExceptionHandler;
import com.club.service.SysDictDataService;
import com.club.service.SysDictTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SysDictTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.mvc.throw-exception-if-no-handler-found=true",
    "spring.web.resources.add-mappings=false"
})
class GlobalExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysDictTypeService dictTypeService;

    @MockBean
    private SysDictDataService dictDataService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private SecurityExceptionHandler securityExceptionHandler;

    @Test
    void whenBusinessException_thenReturnsCode500() throws Exception {
        when(dictTypeService.listPage(any(), any(), any()))
                .thenThrow(new BusinessException("参数错误"));

        mockMvc.perform(get("/system/dict/type/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("参数错误"));
    }

    @Test
    void whenNotFound_thenReturns404() throws Exception {
        mockMvc.perform(get("/nonexistent/path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}
