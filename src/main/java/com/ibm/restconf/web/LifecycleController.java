package com.ibm.restconf.web;

import com.ibm.restconf.model.ExecutionAcceptedResponse;
import com.ibm.restconf.model.ExecutionRequest;
import com.ibm.restconf.security.AccessDeniedException;
import com.ibm.restconf.service.LifecycleManagementService;
import com.ibm.restconf.service.MessageConversionException;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import static com.ibm.restconf.config.RCDriverConstants.TENANTID;

@RestController("LifecycleController")
@RequestMapping("/api/driver")
public class LifecycleController {

    private final static Logger logger = LoggerFactory.getLogger(LifecycleController.class);

    private final LifecycleManagementService lifecycleManagementService;

    @Autowired
    public LifecycleController(final LifecycleManagementService lifecycleManagementService) {
        this.lifecycleManagementService = lifecycleManagementService;
    }

    @PostMapping("/lifecycle/execute")
    @Operation(summary = "Execute a lifecycle against a RestConf", description = "Initiates a lifecycle ")
    public ResponseEntity<ExecutionAcceptedResponse> executeLifecycle(@RequestBody ExecutionRequest executionRequest, @RequestHeader(value = TENANTID, required = false) String tenantId, HttpServletRequest servletRequest) throws MessageConversionException, AccessDeniedException {
        /*try (BufferedReader messageReader = servletRequest.getReader()) {
            String rawMessage = messageReader.lines().collect(Collectors.joining("\n"));
            logger.info("Received ExecutionRequest:\n{}", rawMessage);
        } catch (IOException e) {
            logger.warn(String.format("Exception caught logging ExecutionRequest message: %s", e.getMessage()), e);
        }*/
        logger.info("Received request to execute a lifecycle [{}] ", executionRequest.getLifecycleName());
        //executionRequest.setTenantId(tenantId);
        tenantId = StringUtils.defaultIfEmpty(tenantId, "1");
        final ExecutionAcceptedResponse responseData = lifecycleManagementService.executeLifecycle(executionRequest, tenantId);
        if(tenantId.equals("1")){
            return ResponseEntity.accepted().body(responseData);
        }else{
            return ResponseEntity.accepted().headers(prepareHttpHeadersWithTenantId(tenantId)).body(responseData);
        }
    }
    private HttpHeaders prepareHttpHeadersWithTenantId(String tenantId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(TENANTID, tenantId);
        logger.info("httpserver {} ", httpHeaders.toString());
        return httpHeaders;
    }
}