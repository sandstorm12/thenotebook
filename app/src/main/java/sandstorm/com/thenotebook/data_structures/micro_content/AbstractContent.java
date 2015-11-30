package sandstorm.com.thenotebook.data_structures.micro_content;

public abstract class AbstractContent
{
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_FILE = "FILE";
    public static final String TYPE_SOUND = "SOUND";
    public static final String TYPE_VIDEO = "VIDEO";

    public abstract String getType();

    public abstract int getSize();

    public abstract Class getTypeClass();
}
