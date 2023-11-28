package model;

public class SanPhamCT {

    private Integer id;
    private Integer id_sanPham;
    private Integer id_HinhDang;
    private Integer id_mauSac;
    private Integer id_Anh;
    private Double gia;
    private Integer soLuong;
    private Integer trangThai;
    private SanPham sanPham;
    private MauSac mauSac;
    private HinhAnh hinhAnh;
    private HinhDang hinhDang;

    public SanPhamCT() {
    }

    public SanPhamCT(Integer id, Integer id_sanPham, Integer id_HinhDang, Integer id_mauSac, Integer id_Anh, Double gia, Integer soLuong, Integer trangThai, SanPham sanPham, MauSac mauSac, HinhAnh hinhAnh, HinhDang hinhDang) {
        this.id = id;
        this.id_sanPham = id_sanPham;
        this.id_HinhDang = id_HinhDang;
        this.id_mauSac = id_mauSac;
        this.id_Anh = id_Anh;
        this.gia = gia;
        this.soLuong = soLuong;
        this.trangThai = trangThai;
        this.sanPham = sanPham;
        this.mauSac = mauSac;
        this.hinhAnh = hinhAnh;
        this.hinhDang = hinhDang;
    }

    public SanPhamCT(SanPham sanPham, MauSac mauSac, HinhDang hinhDang) {
        this.sanPham = sanPham;
        this.mauSac = mauSac;
        this.hinhDang = hinhDang;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId_sanPham() {
        return id_sanPham;
    }

    public void setId_sanPham(Integer id_sanPham) {
        this.id_sanPham = id_sanPham;
    }

    public Integer getId_HinhDang() {
        return id_HinhDang;
    }

    public void setId_HinhDang(Integer id_HinhDang) {
        this.id_HinhDang = id_HinhDang;
    }

    public Integer getId_mauSac() {
        return id_mauSac;
    }

    public void setId_mauSac(Integer id_mauSac) {
        this.id_mauSac = id_mauSac;
    }

    public Integer getId_Anh() {
        return id_Anh;
    }

    public void setId_Anh(Integer id_Anh) {
        this.id_Anh = id_Anh;
    }

    public Double getGia() {
        return gia;
    }

    public void setGia(Double gia) {
        this.gia = gia;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public MauSac getMauSac() {
        return mauSac;
    }

    public void setMauSac(MauSac mauSac) {
        this.mauSac = mauSac;
    }

    public HinhAnh getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(HinhAnh hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public HinhDang getHinhDang() {
        return hinhDang;
    }

    public void setHinhDang(HinhDang hinhDang) {
        this.hinhDang = hinhDang;
    }

    public String loadTrangThai() {
        if (trangThai == 1) {
            return "Đang bán";
        }
        return "Ngừng bán";
    }
}
