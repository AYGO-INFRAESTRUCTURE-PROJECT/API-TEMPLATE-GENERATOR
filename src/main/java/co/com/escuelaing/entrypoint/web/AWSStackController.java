package co.com.escuelaing.entrypoint.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import co.com.escuelaing.model.WebStack;
import co.com.escuelaing.services.TemplateService;

import javax.validation.Valid;

import java.io.BufferedReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "v1")
public class AWSStackController {
    private final TemplateService service;

    public AWSStackController(TemplateService service) {
        this.service = service;
    }

    @PostMapping("/synth")
    @PreAuthorize("hasAuthority('create:template')")
    public ResponseEntity<String> synthTemplate(@RequestBody WebStack stack) throws Exception {
        String template = service.synthStack(stack);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(template);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        Map<String, String> map = new HashMap<>(errors.size());
        errors.forEach((error) -> {
            String key = ((FieldError) error).getField();
            String val = error.getDefaultMessage();
            map.put(key, val);
        });
        return ResponseEntity.badRequest().body(map);
    }
}