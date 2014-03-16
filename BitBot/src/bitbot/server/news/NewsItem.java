package bitbot.server.news;

/**
 *
 * @author z
 */
public class NewsItem {

    private final String Title, ImageUri, Desc, LinkUri, PublishDate, Source;

    public NewsItem(String _Title, String _ImageUri, String _Desc, String _LinkUri, String _PublishDate, String _Source) {
        Title = _Title;
        ImageUri = _ImageUri;
        Desc = _Desc;
        LinkUri = _LinkUri;
        PublishDate = _PublishDate;
        Source = _Source;
    }

    public String getTitle() {
        return Title;
    }

    public String getImageUri() {
        return ImageUri;
    }

    public String getDesc() {
        return Desc;
    }

    public String getLinkUri() {
        return LinkUri;
    }

    public String getPublishDate() {
        return PublishDate;
    }

    public String getSource() {
        return Source;
    }
}
