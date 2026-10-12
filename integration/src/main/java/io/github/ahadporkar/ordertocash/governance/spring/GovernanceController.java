package io.github.ahadporkar.ordertocash.governance.spring;
import io.github.ahadporkar.ordertocash.governance.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
/** Shares the tested application router; HTTP container behavior is a separate acceptance gate. */
@RestController
@ConditionalOnProperty(name="app.governance.enabled",havingValue="true")
@RequestMapping("/governance/api")
public class GovernanceController {
    private final ApiRouter router;
    public GovernanceController(ApiRouter router){this.router=router;}
    @RequestMapping(value={"","/**"})
    public ResponseEntity<Map<String,Object>> request(HttpServletRequest request) throws IOException {
        ApiRouter.Response result;
        try {
            Problem.require(request.getQueryString()==null,400,"QUERY_NOT_SUPPORTED");
            Map<String,Object> body=Map.of();
            if("POST".equals(request.getMethod())) {
                String type=request.getContentType();
                Problem.require(type!=null&&type.split(";",2)[0].trim().equalsIgnoreCase("application/json"),415,"JSON_REQUIRED");
                byte[] bytes=request.getInputStream().readNBytes(16385);
                Problem.require(bytes.length<=16384,413,"BODY_TOO_LARGE");
                try {
                    String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
                    body=Json.object(Json.parse(text));
                }catch(IllegalArgumentException|CharacterCodingException e){throw new Problem(400,"INVALID_JSON");}
            }
            String local=request.getRequestURI().substring(request.getContextPath().length());
            result=router.route(request.getMethod(),local.substring("/governance".length()),
                    request.getHeader("Authorization"),request.getHeader("Idempotency-Key"),body);
        }catch(Problem p){result=ApiRouter.problem(p);}
        catch(RuntimeException e){result=ApiRouter.problem(new Problem(503,"SERVICE_UNAVAILABLE"));}
        return ResponseEntity.status(result.status()).contentType(result.status()>=400?MediaType.APPLICATION_PROBLEM_JSON:MediaType.APPLICATION_JSON).body(result.body());
    }
}
