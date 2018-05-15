package innkfx.strategies.catchthechaos.common;

public enum ChPerformsType {
    FRACTAL_UP("FRACTAL_UP"),
    FRACTAL_DOWN("FRACTAL_DOWN"),

    AWESOME_UP("AWESOME_UP"),
    AWESOME_DOWN("AWESOME_DOWN"),

    ORDER_SIGNAL_UP("ORDER_SIGNAL_UP"),
    ORDER_SIGNAL_DOWN("ORDER_SIGNAL_DOWN");

    private String text;

    ChPerformsType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
            return text;
        }
}
