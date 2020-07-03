package dao;

public class Image {
    private int imageId;
    private String imageName;
    private int size;
    private String uploadTime;
    private String contentType;
    private String path;
    private String md5;//Image类把属性设置成私有，对外不可见，所以要有get（取值） set(赋值)方法

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {  //返回该对象的字符串表示
        return "Image{" +
                "imageId=" + imageId +
                ", imageName='" + imageName + '\'' +
                ", size=" + size +
                ", uploadTime='" + uploadTime + '\'' +
                ", contentType='" + contentType + '\'' +
                ", path='" + path + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
