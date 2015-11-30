package sandstorm.com.thenotebook.data_structures.micro_content;

import android.util.Base64;

public class ContentText extends AbstractContent
{
    private String text = "";
    private String Id = AbstractContent.TYPE_TEXT;

    public ContentText(){}

    public ContentText(String text)
    {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return Id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextEncoded()
    {
        try{
            return Base64.encodeToString(getText().getBytes("UTF-8"),0);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getSize() {
        return 0;
    }

    public byte[] getDataBytes()
    {
        try{
            return text.getBytes("UTF-8");
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Class getTypeClass()
    {
        try{
            return ContentText.class;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
