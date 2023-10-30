package m3.users.commons;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("m3.lib")
@EntityScan("m3.lib.entities")
public class ConfigM3Lib {
}
