package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.RequestHeader;
import digit.web.models.ServiceWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * Request object to fetch the report data
 */
@Schema(description = "Request object to fetch the report data")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T11:15:48.645128889+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequest   {
        @JsonProperty("requestInfo")

          @Valid
                private RequestInfo requestInfo = null;

        @JsonProperty("pgrEntity")
          @NotNull

          @Valid
                private ServiceWrapper pgrEntity = null;

    @Valid
    @NonNull
    @JsonProperty("service")
    private Service service = null;

    @Valid
    @JsonProperty("workflow")
    private Workflow workflow = null;
}
