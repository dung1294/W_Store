package view;

import Service.BillService;
import Service.HinhDangService;
import Service.HoaDonChiTietSerivce;
import Service.KhachHangService;
import Service.MauSacService;
import Service.NhanVienService;
import Service.SanPhamCTService;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.HinhDang;
import model.HoaDon;
import model.HoaDonChiTiet;
import model.MauSac;
import model.NhanVien;
import model.SanPhamCT;
import repository.Auth;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import model.KhachHang;
import repository.Validated;

public class Form_BanHang extends javax.swing.JPanel implements Runnable, ThreadFactory {

    private final BillService hdService = new BillService();
    private final NhanVienService nvService = new NhanVienService();
    private final SanPhamCTService spctService = new SanPhamCTService();
    private final HinhDangService kdService = new HinhDangService();
    private final MauSacService msService = new MauSacService();
    private final HoaDonChiTietSerivce hdctSerivce = new HoaDonChiTietSerivce();
    private final KhachHangService khService = new KhachHangService();
    private int row = -1;
    private int pages = 1;
    private final int limit = 5;
    private int numberOfPages = 0;
    private int check;
    private int canExecute = 0;
    private WebcamPanel webcamPanel = null;
    private Webcam webcam = null;
    private final Executor executor = Executors.newSingleThreadExecutor(this);
    private volatile boolean isRunning = true;

    public Form_BanHang() {
        initComponents();
        this.fillTableHD();
        this.fillTableSP();
        this.updateStatusFilter();
        this.fillCbbHinhDangFilter();
        this.fillCbbMauSacFilter();
        this.loadSearch();
        this.initWebcam();
        String maNV = Auth.user.getTaiKhoan();
        NhanVien nv = nvService.selectByMa(maNV);
        lblTenNV.setText(nv.getTen());
        if (txtSDT.getText() == null
                || txtSDT.getText().trim().isEmpty()) {
            lblTenKH.setText("Khác hàng chưa tồn tại");
        }
        this.loadTienThua();
        this.loadTenKH();
    }

    //Xử lý hóa đơn
    private void fillTableHD() {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        model.setRowCount(0);

        try {
            List<HoaDon> list = hdService.selectByStatus();
            for (int i = 0; i < list.size(); i++) {
                HoaDon hd = list.get(i);
                model.addRow(new Object[]{
                    i + 1,
                    hd.getMa(),
                    hd.getNgayTao(),
                    hd.getNv().getTen(),
                    (hd.getTrangThai() == 1) ? "Đã thanh toán" : "Chờ thanh toán"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn dữ liệu!");
        }
    }

    private void setDataHoaDon(HoaDon hd) {
        lblMaHD.setText(hd.getMa());
        lblNgayMua.setText(String.valueOf(hd.getNgayTao()));
        this.row = tblHoaDon.getSelectedRow();
        String maHD = (String) tblHoaDon.getValueAt(row, 1);
        HoaDon hoaDon = hdService.selectByMa(maHD);
        List<HoaDonChiTiet> list = hdctSerivce.selectByMaHD(hoaDon.getMa());
        Double tongTien = 0.0;
        for (int i = 0; i < list.size(); i++) {
            double giaTri = list.get(i).getTongTien();
            tongTien += giaTri;
        }
        lblTongTien.setText(String.valueOf(tongTien));
    }

    private HoaDon getDataBill() {
        HoaDon hd = new HoaDon();

        Date date = new Date();
        hd.setNgayTao(new java.sql.Date(date.getTime()));
        String maNV = Auth.user.getTaiKhoan();
        NhanVien nv = nvService.selectByMa(maNV);
        hd.setIdNV(nv.getId());
        hd.setTongGia(null);
        hd.setIdKH(null);
        Integer trangThai = 2;
        hd.setTrangThai(trangThai);

        return hd;
    }

    private void insertBill() {
        check = JOptionPane.showConfirmDialog(this, "Bạn thực sự muốn tạo hóa đơn mới");
        if (check != JOptionPane.YES_OPTION) {
            return;
        }
        HoaDon hoaDon = this.getDataBill();

        try {
            hdService.insert(hoaDon);
            this.fillTableHD();
            JOptionPane.showMessageDialog(this, "Tạo hóa đơn thành công!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tạo hóa đơn thất bại!");
        }
    }

    private void updateBill() {
        HoaDon hd = new HoaDon();

        String maHD = (String) tblHoaDon.getValueAt(row, 1);
        HoaDon hoaDon = hdService.selectByMa(maHD);

        String sdt = txtSDT.getText();
        KhachHang kh = khService.selectBySDT(sdt);
        hd.setIdKH(kh.getId());

        hd.setId(hoaDon.getId());

        hd.setTongGia(Double.parseDouble(lblTongTien.getText()));

        Integer trangThai = 1;
        hd.setTrangThai(trangThai);

        try {
            hdService.update(hd);
            this.fillTableHD();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đổi trạng thái hóa đơn thất bại!");
        }
    }

    //End xử lý hóa đơn
    //Xử lý sản phẩm
    private void getPages(List<SanPhamCT> list) {
        if (list.size() % limit == 0) {
            numberOfPages = list.size() / limit;
        } else {
            numberOfPages = (list.size() / limit) + 1;
        }

        lblPages.setText("1");
    }

    private void fillTableSP() {
        DefaultTableModel model = (DefaultTableModel) tblSanPham.getModel();
        model.setRowCount(0);

        try {
            String keyWord = txtSearch.getText();

            List<SanPhamCT> listPage = spctService.selectPageStatus(keyWord);
            this.getPages(listPage);

            List<SanPhamCT> list = spctService.searchKeyWordStatus(keyWord, pages, limit);
            for (SanPhamCT spct : list) {
                model.addRow(new Object[]{
                    spct.getId(),
                    spct.getSanPham().getMa(),
                    spct.getSanPham().getTen(),
                    spct.getGia(),
                    spct.getSoLuong(),
                    spct.getMauSac().getTen(),
                    spct.getHinhDang().getTen(),
                    spct.loadTrangThai()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn dữ liệu!");
        }
    }

    private void fillCbbHinhDangFilter() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) cbbFilterKieuDang.getModel();
        model.removeAllElements();
        model.addElement("");

        List<HinhDang> listCbb = kdService.selectAll();
        for (HinhDang hd : listCbb) {
            model.addElement(hd.getTen());
        }
    }

    private void fillCbbMauSacFilter() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) cbbFilterMau.getModel();
        model.removeAllElements();
        model.addElement("");

        List<MauSac> listCbb = msService.selectAll();
        for (MauSac mauSac : listCbb) {
            model.addElement(mauSac.getTen());
        }
    }

    private void loadSearch() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fillTableSP();
                firstPage();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fillTableSP();
                firstPage();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fillTableSP();
                firstPage();
            }
        });
    }

    //Start filter---
    private void filter() {
        DefaultTableModel model = (DefaultTableModel) tblSanPham.getModel();
        model.setRowCount(0);

        try {

            Double giaMin = null;
            if (!txtMin.getText().trim().isEmpty()) {
                giaMin = Double.parseDouble(txtMin.getText());
            }
            Double giaMax = null;
            if (!txtMax.getText().trim().isEmpty()) {
                giaMax = Double.parseDouble(txtMax.getText());
            }

            String keyWord = txtSearch.getText();

            String mau = (String) cbbFilterMau.getSelectedItem();

            String hinhDang = (String) cbbFilterKieuDang.getSelectedItem();

            List<SanPhamCT> listPage = spctService.FilterPage(keyWord, giaMin, giaMax, mau, hinhDang);
            this.getPages(listPage);

            List<SanPhamCT> list = spctService.FilterData(keyWord, giaMin, giaMax, mau, hinhDang, pages, limit);
            for (SanPhamCT spct : list) {
                model.addRow(new Object[]{
                    spct.getId(),
                    spct.getSanPham().getMa(),
                    spct.getSanPham().getTen(),
                    spct.getGia(),
                    spct.getSoLuong(),
                    spct.getMauSac().getTen(),
                    spct.getHinhDang().getTen(),
                    spct.loadTrangThai()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn dữ liệu!");
        }
    }
//End filter---

    private void updateStatusFilter() {
        Boolean checkStatus = (canExecute == 1);
        btnClean.setEnabled(checkStatus);
    }

    //Start phân trang---
    private void firstPage() {
        pages = 1;
        if (canExecute == 1) {
            this.filter();
        } else {
            this.fillTableSP();
        }

        lblPages.setText("1");
    }

    private void prevPage() {
        if (pages > 1) {
            pages--;
            if (canExecute == 1) {
                this.filter();
            } else {
                this.fillTableSP();
            }

            lblPages.setText("" + pages);
        }
    }

    private void nextPage() {
        if (pages < numberOfPages) {
            pages++;
            if (canExecute == 1) {
                this.filter();
            } else {
                this.fillTableSP();
            }

            lblPages.setText("" + pages);
        }
    }

    private void lastPage() {
        pages = numberOfPages;
        if (canExecute == 1) {
            this.filter();
        } else {
            this.fillTableSP();
        }

        lblPages.setText("" + pages);
    }
//End phân trang---

    private SanPhamCT updateSoLuongSP(Integer soLuong) {
        SanPhamCT spct = new SanPhamCT();

        this.row = tblSanPham.getSelectedRow();
        Integer idSP = (Integer) tblSanPham.getValueAt(row, 0);
        SanPhamCT spctUpdate = spctService.selectById(idSP);

        Integer slMoi = spctUpdate.getSoLuong() - soLuong;

        Integer trangThai;
        if (slMoi == 0) {
            trangThai = 2;
        } else {
            trangThai = 1;
        }
        spct.setTrangThai(trangThai);
        spct.setSoLuong(slMoi);

        spct.setId(spctUpdate.getId());

        return spct;
    }

    private void updateDataProducts(SanPhamCT spct) {
        try {
            spctService.updateSoLuong(spct);
            this.fillTableSP();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update số lượng thất bại!");
        }
    }
//End Xử lý sản phẩm

    //Xử lý giỏ hàng
    private void fillTableGioHang(HoaDon hoaDon) {
        DefaultTableModel model = (DefaultTableModel) tblGioHang.getModel();
        model.setRowCount(0);

        try {
            List<HoaDonChiTiet> list = hdctSerivce.selectByMaHD(hoaDon.getMa());
            for (HoaDonChiTiet hdct : list) {
                model.addRow(new Object[]{
                    hdct.getId(),
                    hdct.getSpct().getSanPham().getMa(),
                    hdct.getSpct().getSanPham().getTen(),
                    hdct.getSpct().getMauSac().getTen(),
                    hdct.getSpct().getHinhDang().getTen(),
                    hdct.getGia(),
                    hdct.getSoLuong(),
                    hdct.getTongTien()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn dữ liệu!");
        }
    }

    private HoaDonChiTiet getDataCart(Integer soLuong) {
        HoaDonChiTiet hdct = new HoaDonChiTiet();

        this.row = tblSanPham.getSelectedRow();
        Integer idSP = (Integer) tblSanPham.getValueAt(row, 0);
        SanPhamCT spct = spctService.selectById(idSP);

        this.row = tblHoaDon.getSelectedRow();
        String maHD = (String) tblHoaDon.getValueAt(row, 1);
        HoaDon hoaDon = hdService.selectByMa(maHD);

        Double gia = spct.getGia();
        hdct.setGia(gia);
        hdct.setSoLuong(soLuong);
        hdct.setId_SPCT(spct.getId());
        hdct.setId_HoaDon(hoaDon.getId());

        return hdct;
    }

    private void insertCart(HoaDonChiTiet hdct) {
        try {
            hdctSerivce.insert(hdct);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Thêm vào giỏ hàng thất bại!");
        }
    }

    private void updateCart(Integer soLuong) {
        this.row = tblSanPham.getSelectedRow();
        Integer idSP = (Integer) tblSanPham.getValueAt(row, 0);

        this.row = tblHoaDon.getSelectedRow();
        String maHD = (String) tblHoaDon.getValueAt(row, 1);
        HoaDon hoaDon = hdService.selectByMa(maHD);
        List<HoaDonChiTiet> list = hdctSerivce.selectByMaHD(hoaDon.getMa());

        HoaDonChiTiet hdct = new HoaDonChiTiet();
        hdct.setSoLuong(soLuong);

        for (HoaDonChiTiet hoaDonChiTiet : list) {
            if (Objects.equals(idSP, hoaDonChiTiet.getId_SPCT())) {
                hdct.setId(hoaDonChiTiet.getId());
                break;
            }
        }

        try {
            hdctSerivce.update(hdct);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Thêm vào giỏ hàng thất bại!");
        }
    }

    private void updateCartAndProducr(Integer newQuantity) {
        HoaDonChiTiet hdct = new HoaDonChiTiet();

        this.row = tblGioHang.getSelectedRow();
        Integer idHdct = (Integer) tblGioHang.getValueAt(row, 0);
        HoaDonChiTiet hdctBanDau = hdctSerivce.selectById(idHdct);

        SanPhamCT spct = new SanPhamCT();
        SanPhamCT spctUpdate = spctService.selectById(hdctBanDau.getId_SPCT());

        Integer checkSL = newQuantity;
        try {
            if (newQuantity >= hdctBanDau.getSoLuong()) {
                if (spctUpdate.getSoLuong() == 0) {
                    checkSL = hdctBanDau.getSoLuong();
                }
            } else {
                checkSL = newQuantity;
            }
            hdct.setSoLuong(checkSL);
            hdct.setId(hdctBanDau.getId());
            if (newQuantity == 0) {
                hdctSerivce.delete(idHdct);
            } else {
                hdctSerivce.update(hdct);
            }

            this.row = tblHoaDon.getSelectedRow();
            String maHD = (String) tblHoaDon.getValueAt(row, 1);
            HoaDon hoaDon = hdService.selectByMa(maHD);
            this.fillTableGioHang(hoaDon);

            Integer slThayDoi = checkSL - hdctBanDau.getSoLuong();
            Integer slMoi = spctUpdate.getSoLuong() - slThayDoi;

            Integer trangThai;
            if (slMoi > 0) {
                trangThai = 1;
            } else {
                trangThai = 2;
            }

            spct.setTrangThai(trangThai);
            spct.setSoLuong(slMoi);
            spct.setId(spctUpdate.getId());
            this.updateDataProducts(spct);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update số lượng cart thất bại!");
        }
    }
//END xử lý giỏ hàng

    //Quét qr
    private void initWebcam() {
        Dimension size = new Dimension(176, 144);

        webcam = Webcam.getWebcams().get(0);

        closeWebcam();

        webcam.setViewSize(size);

        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setPreferredSize(size);
        webcamPanel.setFPSDisplayed(true);

        pnlCam.setLayout(new BorderLayout());
        pnlCam.add(webcamPanel, BorderLayout.CENTER);

        executor.execute(this);
    }

    private void closeWebcam() {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    @Override
    public void run() {
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(com.google.zxing.qrcode.encoder.QRCode.class.getName()).log(Level.SEVERE, null, ex);
            }

            Result result = null;
            BufferedImage image = null;

            // Đảm bảo rằng webcam đã mở và có hình ảnh trước khi xử lý
            if (webcam != null && webcam.isOpen() && (image = webcam.getImage()) != null) {
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                SwingUtilities.invokeLater(() -> webcamPanel.repaint());

                try {
                    result = new MultiFormatReader().decode(bitmap);
                } catch (NotFoundException ex) {
                    Logger.getLogger(com.google.zxing.qrcode.encoder.QRCode.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (result != null) {
                    Integer idSP = Integer.parseInt(result.getText());
                    SanPhamCT sanPhamCT = spctService.selectById(idSP);
                    if (sanPhamCT == null) {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm với ID: " + idSP);
                        continue;
                    }

                    String input = JOptionPane.showInputDialog(this, "Nhập số lượng:");
                    if (input == null || input.isEmpty()) {
                        continue;
                    }
                    Integer soLuong = Integer.parseInt(input);
                    if (soLuong < 0) {
                        JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
                        continue;
                    }

                    Integer slsp = sanPhamCT.getSoLuong();

                    if (soLuong > slsp) {
                        JOptionPane.showMessageDialog(this, "Sản phẩm chỉ còn lại" + slsp);
                        soLuong = slsp;
                    }

                    Integer soLuongSp = 0;
                    this.row = tblHoaDon.getSelectedRow();
                    if (row < 0) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để thêm sản phẩm!");
                        continue;
                    }
                    String maHD = (String) tblHoaDon.getValueAt(row, 1);
                    HoaDon hoaDon = hdService.selectByMa(maHD);
                    List<HoaDonChiTiet> list = hdctSerivce.selectByMaHD(hoaDon.getMa());

                    for (HoaDonChiTiet hoaDonChiTiet : list) {
                        if (Objects.equals(idSP, hoaDonChiTiet.getId_SPCT())) {
                            soLuongSp = hoaDonChiTiet.getSoLuong() + soLuong;
                            HoaDonChiTiet hdct = new HoaDonChiTiet();
                            hdct.setSoLuong(soLuongSp);
                            hdct.setId(hoaDonChiTiet.getId());

                            try {
                                hdctSerivce.update(hdct);
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(this, "Thêm vào giỏ hàng thất bại!");
                            }

                            break;
                        }
                    }

                    if (soLuongSp == 0) {
                        soLuongSp = soLuong;

                        HoaDonChiTiet hdct = new HoaDonChiTiet();
                        Double gia = sanPhamCT.getGia();
                        hdct.setGia(gia);
                        hdct.setSoLuong(soLuongSp);
                        hdct.setId_SPCT(sanPhamCT.getId());
                        hdct.setId_HoaDon(hoaDon.getId());

                        this.insertCart(hdct);
                    }

                    //Load table giỏ hàng
                    this.fillTableGioHang(hoaDon);
                    this.setDataHoaDon(hoaDon);

                    SanPhamCT spct = new SanPhamCT();
                    SanPhamCT spctUpdate = spctService.selectById(idSP);

                    Integer slMoi = spctUpdate.getSoLuong() - soLuong;
                    spct.setSoLuong(slMoi);
                    Integer trangThai;
                    if (slMoi == 0) {
                        trangThai = 2;
                    } else {
                        trangThai = 1;
                    }
                    spct.setTrangThai(trangThai);
                    spct.setId(spctUpdate.getId());
                    this.updateDataProducts(spct);
                }
            }
        } while (isRunning);
    }

    public void stopThread() {
        isRunning = false;
        closeWebcam();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "my Thread");
        t.setDaemon(true);
        return t;
    }
    //End quét qr

    //Xử lý thanh toán
    private void fillTenKH() {
        String sdt = txtSDT.getText().trim();

        if (sdt == null || sdt.isEmpty()) {
            lblTenKH.setText("Khách hàng chưa tồn tại");
            return;
        }

        List<KhachHang> list = khService.selectAll();
        boolean found = false;

        for (KhachHang kh : list) {
            if (sdt.equals(kh.getSdt())) {
                lblTenKH.setText(kh.getTen());
                found = true;
                break;
            }
        }

        if (!found) {
            lblTenKH.setText("Khách hàng chưa tồn tại");
        }
    }

    private void loadTenKH() {
        txtSDT.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fillTenKH();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fillTenKH();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fillTenKH();
            }
        });
    }

    public void fillTienThua() {
        if (!Validated.isNumericDouble(txtTienTra.getText())) {
            return;
        }
        Double tienTra = Double.parseDouble(txtTienTra.getText());
        Double tongTien = Double.parseDouble(lblTongTien.getText());
        Double tienThua = tongTien - tienTra;
        if (tienTra <= tongTien) {
            tienThua = 0.0;
        } else {
            tienThua = -tienThua;
        }
        lblTienThua.setText(String.valueOf(tienThua));
    }

    private void loadTienThua() {
        txtTienTra.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fillTienThua();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fillTienThua();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fillTienThua();
            }
        });
    }

    private void ThanhToan() {
        if (lblMaHD.getText().trim().isEmpty() || lblMaHD.getText() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để thanh toán!");
            return;
        }

        if (lblTongTien.getText().trim().isEmpty()
                || lblTongTien.getText() == null
                || Double.parseDouble(lblTongTien.getText()) == 0.0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để thanh toán!");
            return;
        }

        List<KhachHang> list = khService.selectAll();
        for (KhachHang khachHang : list) {
            if (!txtSDT.getText().trim().equals(khachHang.getSdt())) {
                JOptionPane.showMessageDialog(this, "Khách hàng khồng tồn tại");
            }
            return;
        }

        if (txtTienTra.getText().trim().isEmpty() || txtTienTra.getText() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập vào tiền trả!");
            return;
        }

        if (Double.parseDouble(lblTongTien.getText()) > Double.parseDouble(txtTienTra.getText())) {
            JOptionPane.showMessageDialog(this, "Vui lòng trả đủ tiền để thanh toán!");
            return;
        }
        this.updateBill();
        lblMaHD.setText("");
        txtSDT.setText("");
        lblNgayMua.setText("");
        lblTongTien.setText("");
        txtTienTra.setText("");
        lblTienThua.setText("");
        DefaultTableModel model = (DefaultTableModel) tblGioHang.getModel();
        model.setRowCount(0);
        JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
    }
    //Emd thanh toán
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHoaDon = new javax.swing.JTable();
        btnTaoHD = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblGioHang = new javax.swing.JTable();
        btnXoaCart = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblSanPham = new javax.swing.JTable();
        jLabel15 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnFirst_Product = new javax.swing.JButton();
        btnPrev_Product = new javax.swing.JButton();
        lblPages = new javax.swing.JLabel();
        btnNext_Product = new javax.swing.JButton();
        btnLast_Product = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        txtMin = new javax.swing.JTextField();
        txtMax = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        cbbFilterMau = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        cbbFilterKieuDang = new javax.swing.JComboBox<>();
        btnFilter = new javax.swing.JButton();
        btnClean = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblNgayMua = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblTenNV = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblTongTien = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtTienTra = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        lblTienThua = new javax.swing.JLabel();
        btnThanhToan = new javax.swing.JButton();
        lblMaHD = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtSDT = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        lblTenKH = new javax.swing.JLabel();
        btnKhachHang = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        pnlCam = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(1020, 700));

        jPanel1.setPreferredSize(new java.awt.Dimension(1000, 700));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel1.setText("Quản Lý Bán Hàng");

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tblHoaDon.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "STT", "Mã HĐ", "Ngày Tạo", "Tên NV", "Trang Thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblHoaDon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHoaDonMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblHoaDon);

        btnTaoHD.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnTaoHD.setText("Tạo Hóa Đơn");
        btnTaoHD.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        btnTaoHD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTaoHDActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnTaoHD, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 96, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnTaoHD, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 106, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("Giỏ Hàng");

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tblGioHang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Mã SP", "Tên SP", "Màu sắc", "kiểu dáng", "Giá", "Số lượng", "Thành tiền"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblGioHang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblGioHangMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblGioHang);

        btnXoaCart.setText("Xóa");
        btnXoaCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaCartActionPerformed(evt);
            }
        });

        btnSua.setText("Sửa");
        btnSua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnXoaCart)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSua)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXoaCart)
                    .addComponent(btnSua))
                .addGap(8, 8, 8))
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Sản Phẩm");

        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        tblSanPham.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Mã SP", "Tên SP", "Giá Bán", "Số Lượng SP", "Màu sắc", "Kiểu dáng", "Trạng thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSanPham.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSanPhamMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tblSanPham);

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel15.setText("Search");

        txtSearch.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        btnFirst_Product.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnFirst_Product.setText("<<");
        btnFirst_Product.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirst_ProductActionPerformed(evt);
            }
        });

        btnPrev_Product.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnPrev_Product.setText("<");
        btnPrev_Product.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrev_ProductActionPerformed(evt);
            }
        });

        lblPages.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        btnNext_Product.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnNext_Product.setText(">");
        btnNext_Product.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNext_ProductActionPerformed(evt);
            }
        });

        btnLast_Product.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnLast_Product.setText(">>");
        btnLast_Product.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLast_ProductActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel11.setText("Khoảng giá:");

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel14.setText("Màu:");

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel16.setText("Kiểu dáng:");

        btnFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Filters.png"))); // NOI18N
        btnFilter.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });

        btnClean.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Clean.png"))); // NOI18N
        btnClean.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        btnClean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCleanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnFirst_Product)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnPrev_Product)
                        .addGap(10, 10, 10)
                        .addComponent(lblPages, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNext_Product)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLast_Product)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(18, 18, 18)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtMin, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtMax, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbbFilterMau, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbbFilterKieuDang, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClean, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel15)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel11)
                        .addComponent(txtMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel14)
                        .addComponent(cbbFilterMau, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel16)
                        .addComponent(cbbFilterKieuDang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnClean, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnFirst_Product)
                        .addComponent(btnPrev_Product))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnNext_Product)
                        .addComponent(btnLast_Product))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblPages, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setText("Mã HĐ");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel6.setText("Ngày mua");

        lblNgayMua.setBackground(new java.awt.Color(255, 255, 255));

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel8.setText("Tên NV");

        lblTenNV.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel10.setText("Tổng tiền");

        lblTongTien.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel12.setText("Tiền đã trả");

        txtTienTra.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel13.setText("Tiền thừa");

        lblTienThua.setBackground(new java.awt.Color(255, 255, 255));

        btnThanhToan.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnThanhToan.setText("Thanh Toán");
        btnThanhToan.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        btnThanhToan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThanhToanActionPerformed(evt);
            }
        });

        lblMaHD.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setText("SDT");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel9.setText("Tên KH");

        btnKhachHang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/adds.png"))); // NOI18N
        btnKhachHang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKhachHangActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNgayMua, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTenNV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTongTien, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTienThua, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtTienTra)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMaHD, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTenKH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnKhachHang, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(lblMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnKhachHang, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(lblTenKH, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblNgayMua, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(5, 5, 5)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTenNV, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(5, 5, 5)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10)
                    .addComponent(lblTongTien, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtTienTra, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13)
                    .addComponent(lblTienThua, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Hóa Đơn");

        pnlCam.setBackground(new java.awt.Color(255, 255, 255));
        pnlCam.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout pnlCamLayout = new javax.swing.GroupLayout(pnlCam);
        pnlCam.setLayout(pnlCamLayout);
        pnlCamLayout.setHorizontalGroup(
            pnlCamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 174, Short.MAX_VALUE)
        );
        pnlCamLayout.setVerticalGroup(
            pnlCamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 142, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(204, 204, 204))
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1)
                                .addGap(190, 190, 190)))
                        .addComponent(pnlCam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(4, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(jLabel3))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pnlCam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1020, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 10, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 10, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 700, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblHoaDonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblHoaDonMouseClicked
        this.row = tblHoaDon.getSelectedRow();
        String maHD = (String) tblHoaDon.getValueAt(row, 1);
        HoaDon hoaDon = hdService.selectByMa(maHD);
        this.fillTableGioHang(hoaDon);
        this.setDataHoaDon(hoaDon);
    }//GEN-LAST:event_tblHoaDonMouseClicked

    private void btnTaoHDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaoHDActionPerformed
        this.insertBill();
    }//GEN-LAST:event_btnTaoHDActionPerformed

    private void tblGioHangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblGioHangMouseClicked

    }//GEN-LAST:event_tblGioHangMouseClicked

    private void btnXoaCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaCartActionPerformed
        this.row = tblGioHang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm trong giỏ hàng!");
            return;
        }

        Integer idHDCT = (Integer) tblGioHang.getValueAt(row, 0);
        HoaDonChiTiet hdctBanDau = hdctSerivce.selectById(idHDCT);
        SanPhamCT spctUpdate = spctService.selectById(hdctBanDau.getId_SPCT());

        SanPhamCT spct = new SanPhamCT();
        try {
            hdctSerivce.delete(idHDCT);
            this.row = tblHoaDon.getSelectedRow();
            String maHD = (String) tblHoaDon.getValueAt(row, 1);
            HoaDon hoaDon = hdService.selectByMa(maHD);
            this.fillTableGioHang(hoaDon);

            Integer slMoi = spctUpdate.getSoLuong() + hdctBanDau.getSoLuong();
            spct.setSoLuong(slMoi);
            spct.setId(spctUpdate.getId());
            this.updateDataProducts(spct);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Xóa thất bại!");
        }
    }//GEN-LAST:event_btnXoaCartActionPerformed

    private void tblSanPhamMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSanPhamMouseClicked
        if (evt.getClickCount() == 2) {

            String input = JOptionPane.showInputDialog(this, "Nhập số lượng:");
            if (input == null || input.isEmpty()) {
                return;
            }
            Integer soLuong = Integer.parseInt(input);
            if (soLuong < 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
                return;
            }

            this.row = tblSanPham.getSelectedRow();
            Integer slsp = (Integer) tblSanPham.getValueAt(row, 4);
            if (soLuong > slsp) {
                JOptionPane.showMessageDialog(this, "Sản phẩm chỉ còn lại " + slsp);
                soLuong = slsp;
            }

            //Thêm sp vào giỏ hàng
            Integer soLuongSp = 0;
            this.row = tblHoaDon.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để thêm sản phẩm!");
                return;
            }
            String maHD = (String) tblHoaDon.getValueAt(row, 1);
            HoaDon hoaDon = hdService.selectByMa(maHD);
            List<HoaDonChiTiet> list = hdctSerivce.selectByMaHD(hoaDon.getMa());

            Integer idSP = (Integer) tblSanPham.getValueAt(row, 0);

            for (HoaDonChiTiet hoaDonChiTiet : list) {
                if (Objects.equals(idSP, hoaDonChiTiet.getId_SPCT())) {
                    soLuongSp = hoaDonChiTiet.getSoLuong() + soLuong;
                    this.updateCart(soLuongSp);
                    break;
                }
            }

            if (soLuongSp == 0) {
                soLuongSp = soLuong;
                HoaDonChiTiet hdct = this.getDataCart(soLuongSp);
                this.insertCart(hdct);
            }

            //Load table giỏ hàng
            this.fillTableGioHang(hoaDon);
            this.setDataHoaDon(hoaDon);

            //update lại số lượng sản phẩm
            SanPhamCT spctUpdate = this.updateSoLuongSP(soLuong);
            this.updateDataProducts(spctUpdate);
        }
    }//GEN-LAST:event_tblSanPhamMouseClicked

    private void btnFirst_ProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirst_ProductActionPerformed
        this.firstPage();
    }//GEN-LAST:event_btnFirst_ProductActionPerformed

    private void btnPrev_ProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrev_ProductActionPerformed
        this.prevPage();
    }//GEN-LAST:event_btnPrev_ProductActionPerformed

    private void btnNext_ProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNext_ProductActionPerformed
        this.nextPage();
    }//GEN-LAST:event_btnNext_ProductActionPerformed

    private void btnLast_ProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLast_ProductActionPerformed
        this.lastPage();
    }//GEN-LAST:event_btnLast_ProductActionPerformed

    private void btnThanhToanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThanhToanActionPerformed
        this.ThanhToan();
    }//GEN-LAST:event_btnThanhToanActionPerformed

    private void btnKhachHangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKhachHangActionPerformed
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            KhachHangJDialog khachHangDialog = new KhachHangJDialog(frame, true);
            khachHangDialog.setVisible(true);
        }

        List<KhachHang> list = khService.selectAll();
        if (!list.isEmpty()) {
            KhachHang lastKhachHang = list.get(list.size() - 1);
            txtSDT.setText(lastKhachHang.getSdt());
        }
    }//GEN-LAST:event_btnKhachHangActionPerformed

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        // TODO add your handling code here:
        canExecute = 1;
        String mau = (String) cbbFilterMau.getSelectedItem();
        String kieuDang = (String) cbbFilterKieuDang.getSelectedItem();

        if (mau.trim().isEmpty()
                && kieuDang.trim().isEmpty()
                && txtMin.getText().trim().isEmpty()
                && txtMax.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầu mục muốn lọc!");
            return;
        }

        if (!txtMin.getText().trim().isEmpty()
                && !txtMax.getText().trim().isEmpty()) {
            if (Double.parseDouble(txtMax.getText()) < Double.parseDouble(txtMin.getText())) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập min nhỏ hơn giá max!");
                return;
            }
        }

        if (!txtMin.getText().trim().isEmpty()) {
            if (Double.parseDouble(txtMin.getText()) < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập giá lớn hơn 0!");
                return;
            }
        }

        if (!txtMax.getText().trim().isEmpty()) {
            if (Double.parseDouble(txtMax.getText()) < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập giá lớn hơn 0!");
                return;
            }
        }

        this.filter();
        this.updateStatusFilter();
    }//GEN-LAST:event_btnFilterActionPerformed

    private void btnCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCleanActionPerformed
        // TODO add your handling code here:
        canExecute = 0;
        this.fillTableSP();
        txtMax.setText("");
        txtMin.setText("");
        cbbFilterKieuDang.setSelectedIndex(0);
        cbbFilterMau.setSelectedIndex(0);
        this.updateStatusFilter();
    }//GEN-LAST:event_btnCleanActionPerformed

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        this.row = tblGioHang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm trong giỏ hàng để sửa!");
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Nhập số lượng:");
        if (input == null || input.isEmpty()) {
            return;
        }
        Integer soLuong = Integer.parseInt(input);
        if (soLuong < 0) {
            JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
            return;
        }
        this.updateCartAndProducr(soLuong);
        this.row = tblHoaDon.getSelectedRow();
        String maHD = (String) tblHoaDon.getValueAt(row, 1);
        HoaDon hoaDon = hdService.selectByMa(maHD);
        this.setDataHoaDon(hoaDon);
    }//GEN-LAST:event_btnSuaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClean;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnFirst_Product;
    private javax.swing.JButton btnKhachHang;
    private javax.swing.JButton btnLast_Product;
    private javax.swing.JButton btnNext_Product;
    private javax.swing.JButton btnPrev_Product;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnTaoHD;
    private javax.swing.JButton btnThanhToan;
    private javax.swing.JButton btnXoaCart;
    private javax.swing.JComboBox<String> cbbFilterKieuDang;
    private javax.swing.JComboBox<String> cbbFilterMau;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblMaHD;
    private javax.swing.JLabel lblNgayMua;
    private javax.swing.JLabel lblPages;
    private javax.swing.JLabel lblTenKH;
    private javax.swing.JLabel lblTenNV;
    private javax.swing.JLabel lblTienThua;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JPanel pnlCam;
    private javax.swing.JTable tblGioHang;
    private javax.swing.JTable tblHoaDon;
    private javax.swing.JTable tblSanPham;
    private javax.swing.JTextField txtMax;
    private javax.swing.JTextField txtMin;
    private javax.swing.JTextField txtSDT;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtTienTra;
    // End of variables declaration//GEN-END:variables
}
