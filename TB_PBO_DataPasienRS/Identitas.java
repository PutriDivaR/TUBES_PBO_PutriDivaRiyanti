import java.text.SimpleDateFormat;
import java.util.Date;

public class Identitas {
    // Atribut umum
    private String id;
    private String nama;
    private String gender;
    private Date tanggalLahir;

    // Constructor
    public Identitas(String id, String nama, String gender, Date tanggalLahir) {
        this.id = id;
        this.nama = nama;
        this.gender = gender;
        this.tanggalLahir = tanggalLahir;
    }

    // Method manipulasi String
    public String formatNama() {
        return nama.toUpperCase();
    }

    // Method manipulasi Date
    public String formatTanggalLahir() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        return formatter.format(tanggalLahir);
    }

    // Getters
    public String getId() { 
        return id; 
    }
    public String getNama() { 
        return nama; 
    }
    public String getGender() { 
        return gender; 
    }
    public Date getTanggalLahir() { 
        return tanggalLahir; 
    }

    // Setters
    public void setNama(String nama) { 
        this.nama = nama; 
    }
    public void setGender(String gender) { 
        this.gender = gender; 
    }
    public void setTanggalLahir(Date tanggalLahir) { 
        this.tanggalLahir = tanggalLahir; 
    }
}
