import java.util.Date;

public class Pasien extends Identitas implements HitungBMI {
    // Atribut
    private double tinggiBadan; // dalam cm
    private double beratBadan; // dalam kg

    // Constructor
    public Pasien(String id, String nama, String gender, Date tanggalLahir, double tinggiBadan, double beratBadan) {
        super(id, nama, gender, tanggalLahir); // Memanggil constructor dari superclass Identitas
        this.tinggiBadan = tinggiBadan;
        this.beratBadan = beratBadan;
    }

    // Implementasi metode hitungBMI dari interface HitungBMI
    @Override
    public double hitungBMI(double tinggiBadan, double beratBadan) {
        double tinggiMeter = tinggiBadan / 100; // Konversi tinggi cm ke meter
        return beratBadan / (tinggiMeter * tinggiMeter); // Rumus BMI
    }

    // Implementasi metode getStatusBMI dari interface HitungBMI
    @Override
    public String getStatusBMI(double bmi) {
        if (bmi < 18.5) {
            return "Kurus";
        } else if (bmi < 24.9) {
            return "Normal";
        } else if (bmi < 29.9) {
            return "Overweight";
        } else {
            return "Obesitas";
        }
    }

    // Override toString untuk menampilkan data pasien
    @Override
    public String toString() {
        double bmi = hitungBMI(tinggiBadan, beratBadan);
        return "ID: " + getId() +
               "\nNama: " + formatNama() +
               "\nGender: " + getGender() +
               "\nTanggal Lahir: " + formatTanggalLahir() +
               "\nTinggi Badan: " + tinggiBadan + " cm" +
               "\nBerat Badan: " + beratBadan + " kg" +
               "\nBMI: " + String.format("%.2f", bmi) +
               "\nStatus BMI: " + getStatusBMI(bmi);
    }

    // Getters dan Setters
    public double getTinggiBadan() { 
        return tinggiBadan; 
    }
    public void setTinggiBadan(double tinggiBadan) { 
        this.tinggiBadan = tinggiBadan; 
    }
    public double getBeratBadan() { 
        return beratBadan; 
    }
    public void setBeratBadan(double beratBadan) { 
        this.beratBadan = beratBadan; 
    }
}
