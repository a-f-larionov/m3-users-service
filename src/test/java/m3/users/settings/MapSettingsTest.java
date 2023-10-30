package m3.users.settings;

import m3.lib.settings.MapSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapSettingsTest {

    @Test
    void getFirstPointId() {
        // given
        var mapId = 10L;

        // when
        var firstPointId = MapSettings.getFirstPointId(mapId);

        // then
        assertEquals(163L, firstPointId);
    }

    @Test
    void getLastPointId() {
        // given
        var mapId = 10L;

        // when
        var lastPointId = MapSettings.getLastPointId(mapId);

        // then
        assertEquals(180L, lastPointId);
    }
}