import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires a running Redis instance")
class LessonSyncApplicationTests {

    @Test
    void contextLoads() {
    }

}
