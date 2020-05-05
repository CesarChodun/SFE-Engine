package main.java.core.rendering;

/**
 * Class for representing color format, and color space.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class ColorFormatAndSpace {

    /** Color format. */
    public int colorFormat;
    /** Color space. */
    public int colorSpace;

    /**
     * @param colorFormat The color format.
     * @param colorSpace The color space.
     */
    public ColorFormatAndSpace(int colorFormat, int colorSpace) {
        this.colorFormat = colorFormat;
        this.colorSpace = colorSpace;
    }
}
