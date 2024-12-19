import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ManajemenRS {
    //koneksi database JDBC (database rs_diva)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/rs_diva"; 
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            // Login
            login();
    
            // Menu utama
            while (true) {
                JPanel menuPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                JButton btnPasienBaru = new JButton("Tambah Pasien Baru");
                JButton btnLihatDataPasien = new JButton("Lihat Data Pasien");
                JButton btnUpdatePasien = new JButton("Update Data Pasien");
                JButton btnHapusPasien = new JButton("Hapus Data Pasien");
                JButton btnKeluar = new JButton("Keluar");
    
                btnPasienBaru.addActionListener(e -> tambahPasien(connection));
                btnLihatDataPasien.addActionListener(e -> lihatDataPasien(connection));
                btnUpdatePasien.addActionListener(e -> updatePasien(connection));
                btnHapusPasien.addActionListener(e -> hapusPasien(connection));
                btnKeluar.addActionListener(e -> {
                    JOptionPane.showMessageDialog(null, "Terima kasih telah menggunakan aplikasi!");
                    System.exit(0);
                });
    
                menuPanel.add(btnPasienBaru);
                menuPanel.add(btnLihatDataPasien);
                menuPanel.add(btnUpdatePasien);
                menuPanel.add(btnHapusPasien);
                menuPanel.add(btnKeluar);
    
                JOptionPane.showOptionDialog(
                    null,
                    menuPanel,
                    "Menu Utama",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{},
                    null
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Koneksi ke database gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //method login
    private static void login() {
        while (true) {
            try {
                JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));
                JTextField usernameField = new JTextField();
                JPasswordField passwordField = new JPasswordField();
                JTextField captchaField = new JTextField();

                String generatedCaptcha = generateCaptcha();

                loginPanel.add(new JLabel("Username:"));
                loginPanel.add(usernameField);
                loginPanel.add(new JLabel("Password:"));
                loginPanel.add(passwordField);
                loginPanel.add(new JLabel("Captcha (" + generatedCaptcha + "):"));
                loginPanel.add(captchaField);

                int loginResult = JOptionPane.showConfirmDialog(
                        null,
                        loginPanel,
                        "Log in",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (loginResult != JOptionPane.OK_OPTION) {
                    JOptionPane.showMessageDialog(null, "Login dibatalkan!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }

                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String captcha = captchaField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    throw new Exception("Username dan Password tidak boleh kosong!");
                }

                if (!captcha.equals(generatedCaptcha)) {
                    throw new Exception("Captcha tidak sesuai!");
                }

                JOptionPane.showMessageDialog(null, "Login berhasil!", "Info", JOptionPane.INFORMATION_MESSAGE);
                break;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }

        }
        
    }

    //method pembuat kode captcha (materi perhitungan mtk)
    private static String generateCaptcha() {
        String chars = "1234567890";
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            captcha.append(chars.charAt(randomIndex));
        }
        return captcha.toString();
    }

    //MATERI CRUD
    //method tambah pasien baru (CREATE)
    private static void tambahPasien(Connection connection) {
    try {
        // Input nama dokter untuk setiap pasien baru
        String namaDokter = JOptionPane.showInputDialog(null, "Masukkan nama dokter:", "Nama Dokter", JOptionPane.INFORMATION_MESSAGE);
        
        // Jika tombol "Cancel" ditekan atau input null, langsung keluar
            if (namaDokter == null) {
                return;
            }
        
            if (namaDokter.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Nama Dokter tidak boleh kosong.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    

        // Input data pasien
        JTextField idField = new JTextField();
        JTextField namaField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField tanggalLahirField = new JTextField();
        JTextField tinggiField = new JTextField();
        JTextField beratField = new JTextField();

        Object[] form = {
            "ID Pasien:", idField,
            "Nama:", namaField,
            "Gender (L/P):", genderField,
            "Tanggal Lahir (yyyy-MM-dd):", tanggalLahirField,
            "Tinggi Badan (cm):", tinggiField,
            "Berat Badan (kg):", beratField
        };

        int result = JOptionPane.showConfirmDialog(null, form, "Tambah Pasien Baru", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText();
            String nama = namaField.getText();
            String gender = genderField.getText().toUpperCase();
            String tanggalLahirStr = tanggalLahirField.getText();
            double tinggiBadan = Double.parseDouble(tinggiField.getText());
            double beratBadan = Double.parseDouble(beratField.getText());

            if (!gender.equals("L") && !gender.equals("P")) {
                throw new IllegalArgumentException("Gender harus L atau P.");
            }

            // Validasi tanggal lahir
            Date tanggalLahir = new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(tanggalLahirStr).getTime());

            // Hitung BMI
            Pasien pasien = new Pasien(id, nama, gender, tanggalLahir, tinggiBadan, beratBadan);
            double bmi = pasien.hitungBMI(tinggiBadan, beratBadan);
            String statusBMI = pasien.getStatusBMI(bmi);

            // Simpan data ke database
            String insertQuery = "INSERT INTO pasien (id_pasien, nama, gender, tanggal_lahir, tinggi_badan, berat_badan, status_bmi, dokter_pemeriksa) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, id);
                statement.setString(2, nama);
                statement.setString(3, gender);
                statement.setDate(4, (java.sql.Date) tanggalLahir);
                statement.setDouble(5, tinggiBadan);
                statement.setDouble(6, beratBadan);
                statement.setString(7, statusBMI);
                statement.setString(8, namaDokter);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(null, "Pasien berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    tampilkanStruk(id, nama, gender, tanggalLahir, tinggiBadan, beratBadan, statusBMI, namaDokter);
                } else {
                    JOptionPane.showMessageDialog(null, "Gagal menambahkan pasien.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Tinggi dan berat badan harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Input tidak valid. Pastikan semua data terisi dengan benar.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}



// Menampilkan struk setelah data pasien berhasil disimpan
private static void tampilkanStruk(String id, String nama, String gender, Date tanggalLahir, double tinggiBadan, double beratBadan, String statusBMI, String namaDokter) {
    //MATERI DATE
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    String formattedDate = now.format(formatter);

    StringBuilder struk = new StringBuilder();
    struk.append("====================================\n");
    struk.append("         HASIL DATA PASIEN\n");
    struk.append("Tanggal dan Waktu : " + formattedDate + "\n"); 
    struk.append("====================================\n");
    struk.append("ID Pasien: " + id + "\n");
    struk.append("Nama: " + nama + "\n");
    struk.append("Gender: " + gender + "\n");
    struk.append("Tanggal Lahir: " + new SimpleDateFormat("yyyy-MM-dd").format(tanggalLahir) + "\n");
    struk.append("Tinggi Badan: " + tinggiBadan + " cm\n");
    struk.append("Berat Badan: " + beratBadan + " kg\n");
    struk.append("Status BMI: " + statusBMI + "\n");
    struk.append("Dokter Pemeriksa: " + namaDokter + "\n");
    struk.append("====================================\n");
    struk.append("Terima kasih telah datang ke RS Diva!\n");
    
    JOptionPane.showMessageDialog(null, struk.toString(), "Struk Pasien", JOptionPane.INFORMATION_MESSAGE);
}

// Lihat Data Pasien (READ)
private static void lihatDataPasien(Connection connection) {
    try {
        String query = "SELECT * FROM pasien";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        // Gunakan ArrayList untuk menyimpan data pasien
        ArrayList<String> daftarPasien = new ArrayList<>();

        while (resultSet.next()) {
            String id = resultSet.getString("id_pasien");
            String nama = resultSet.getString("nama");
            String gender = resultSet.getString("gender");
            Date tanggalLahir = resultSet.getDate("tanggal_lahir");
            double tinggiBadan = resultSet.getDouble("tinggi_badan");
            double beratBadan = resultSet.getDouble("berat_badan");
            String statusBMI = resultSet.getString("status_bmi");
            String dokterPemeriksa = resultSet.getString("dokter_pemeriksa");

            String dataPasien = String.format(
                "ID: %s\nNama: %s\nGender: %s\nTanggal Lahir: %s\nTinggi: %.2f cm\nBerat: %.2f kg\nStatus BMI: %s\nDokter: %s\n",
                id, nama, gender, new SimpleDateFormat("yyyy-MM-dd").format(tanggalLahir), tinggiBadan, beratBadan, statusBMI, dokterPemeriksa
            );

            // Tambahkan data pasien ke ArrayList
            daftarPasien.add(dataPasien);
        }

        // Format hasil dari ArrayList
        StringBuilder result = new StringBuilder();
        result.append("====================================\n");
        result.append("           DAFTAR PASIEN\n");
        result.append("====================================\n");
        for (String pasien : daftarPasien) {
            result.append(pasien);
            result.append("====================================\n");
        }

        JOptionPane.showMessageDialog(null, result.toString(), "Data Pasien", JOptionPane.INFORMATION_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Gagal mengambil data pasien: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Update Data Pasien (UPDATE)
private static void updatePasien(Connection connection) {
    try {
        String idPasien = JOptionPane.showInputDialog("Masukkan ID Pasien yang akan diperbarui:");

        // Jika tombol "Cancel" ditekan atau input null, langsung keluar
        if (idPasien == null) {
            return;
        }
    
        if (idPasien.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "ID Pasien belum diinput.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "SELECT * FROM pasien WHERE id_pasien = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, idPasien);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            JTextField namaField = new JTextField(resultSet.getString("nama"));
            JTextField genderField = new JTextField(resultSet.getString("gender"));
            JTextField tanggalLahirField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(resultSet.getDate("tanggal_lahir")));
            JTextField tinggiField = new JTextField(String.valueOf(resultSet.getDouble("tinggi_badan")));
            JTextField beratField = new JTextField(String.valueOf(resultSet.getDouble("berat_badan")));
            JTextField dokterField = new JTextField(resultSet.getString("dokter_pemeriksa"));

            Object[] form = {
                "Nama:", namaField,
                "Gender (L/P):", genderField,
                "Tanggal Lahir (yyyy-MM-dd):", tanggalLahirField,
                "Tinggi Badan (cm):", tinggiField,
                "Berat Badan (kg):", beratField,
                "Nama Dokter:", dokterField
            };

            int result = JOptionPane.showConfirmDialog(null, form, "Update Data Pasien", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String nama = namaField.getText();
                String gender = genderField.getText().toUpperCase();
                String tanggalLahirStr = tanggalLahirField.getText();
                double tinggiBadan = Double.parseDouble(tinggiField.getText());
                double beratBadan = Double.parseDouble(beratField.getText());
                String dokterPemeriksa = dokterField.getText();

                if (!gender.equals("L") && !gender.equals("P")) {
                    throw new IllegalArgumentException("Gender harus L atau P.");
                }

                Date tanggalLahir = new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(tanggalLahirStr).getTime());
                Pasien pasien = new Pasien(idPasien, nama, gender, tanggalLahir, tinggiBadan, beratBadan);
                double bmi = pasien.hitungBMI(tinggiBadan, beratBadan);
                String statusBMI = pasien.getStatusBMI(bmi);


                String updateQuery = "UPDATE pasien SET nama = ?, gender = ?, tanggal_lahir = ?, tinggi_badan = ?, berat_badan = ?, status_bmi = ?, dokter_pemeriksa = ? WHERE id_pasien = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setString(1, nama);
                    updateStatement.setString(2, gender);
                    updateStatement.setDate(3, (java.sql.Date) tanggalLahir);
                    updateStatement.setDouble(4, tinggiBadan);
                    updateStatement.setDouble(5, beratBadan);
                    updateStatement.setString(6, statusBMI);
                    updateStatement.setString(7, dokterPemeriksa);
                    updateStatement.setString(8, idPasien);

                    int rowsUpdated = updateStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(null, "Data pasien berhasil diperbarui!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Data pasien gagal diperbarui.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Pasien dengan ID tersebut tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException | ParseException | IllegalArgumentException e) {
        JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat memperbarui data pasien: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Hapus Data Pasien (DELETE)
private static void hapusPasien(Connection connection) {
    try {

        String idPasien = JOptionPane.showInputDialog("Masukkan ID Pasien yang akan dihapus:");
        
        // Jika tombol "Cancel" ditekan atau input null, langsung keluar
            if (idPasien == null) {
                return;
            }
        
            if (idPasien.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "ID Pasien belum diinput.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

        String query = "SELECT * FROM pasien WHERE id_pasien = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, idPasien);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus pasien dengan ID: " + idPasien + "?", "Hapus Pasien", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String deleteQuery = "DELETE FROM pasien WHERE id_pasien = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                    deleteStatement.setString(1, idPasien);
                    int rowsDeleted = deleteStatement.executeUpdate();
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(null, "Pasien berhasil dihapus!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Pasien gagal dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Pasien dengan ID tersebut tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Gagal menghapus data pasien: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


}