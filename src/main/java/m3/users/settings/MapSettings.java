package m3.users.settings;

public class MapSettings {

    public static final Long POINTS_PER_MAP = 18L;

    public static Long getFirstPointId(Long mapId) {
        return POINTS_PER_MAP * (mapId - 1) + 1;
    }

    public static Long getLastPointId(Long mapId) {
        return getFirstPointId(mapId) + POINTS_PER_MAP - 1;
    }
}
