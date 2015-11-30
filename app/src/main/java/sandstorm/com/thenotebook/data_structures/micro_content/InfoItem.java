package sandstorm.com.thenotebook.data_structures.micro_content;

public class InfoItem
{
    private String name, content;

    public InfoItem(String name) {
        this.name = name;
    }

    public InfoItem(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
