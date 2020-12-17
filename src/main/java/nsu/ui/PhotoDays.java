package nsu.ui;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

public class PhotoDays {
    private static PhotoDays instance;
    private String date1;
    private String photo1;
    private String date2;
    private String photo2;

    public static synchronized PhotoDays getInstance() {
        if (instance == null) {
            System.out.println("instance = 0");
            instance = new PhotoDays();
        }
        return instance;
    }

    public String getDate1() {
        return this.date1;
    }

    public void setDate1(String date1) {
        this.date1 = date1;
    }

    public String getDate2() {
        return date2;
    }
    public void setDate2(String date2) {
        this.date2 = date2;
    }

    public String getPhoto1() {
        return photo1;
    }

    public void setPhoto1(String photo1) {
        this.photo1 = photo1;
    }
    public String getPhoto2() {
        return photo2;
    }

    public void setPhoto2(String photo2) {
        this.photo2 = photo2;
    }

    public void setInstance(PhotoDays photoDays) {
        instance = photoDays;
    }


}
