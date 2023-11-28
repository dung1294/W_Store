package model;

import java.sql.Date;

public class HoaDon {

    private Integer id;
    private String ma;
    private Date ngayTao;
    private Double tongGia;
    private Integer trangThai;
    private Integer idNV;
    private Integer idKH;
    private Integer idKM;
    private NhanVien nv;
    private KhachHang kh;

    public HoaDon() {
    }

    public HoaDon(Integer id, String ma, Date ngayTao, Double tongGia, Integer trangThai, Integer idNV, Integer idKH, Integer idKM, NhanVien nv, KhachHang kh) {
        this.id = id;
        this.ma = ma;
        this.ngayTao = ngayTao;
        this.tongGia = tongGia;
        this.trangThai = trangThai;
        this.idNV = idNV;
        this.idKH = idKH;
        this.idKM = idKM;
        this.nv = nv;
        this.kh = kh;
    }

    public HoaDon(String ma) {
        this.ma = ma;
    }

    public HoaDon(NhanVien nv) {
        this.nv = nv;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Double getTongGia() {
        return tongGia;
    }

    public void setTongGia(Double tongGia) {
        this.tongGia = tongGia;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getIdNV() {
        return idNV;
    }

    public void setIdNV(Integer idNV) {
        this.idNV = idNV;
    }

    public Integer getIdKH() {
        return idKH;
    }

    public void setIdKH(Integer idKH) {
        this.idKH = idKH;
    }

    public NhanVien getNv() {
        return nv;
    }

    public void setNv(NhanVien nv) {
        this.nv = nv;
    }

    public KhachHang getKh() {
        return kh;
    }

    public void setKh(KhachHang kh) {
        this.kh = kh;
    }

    public Integer getIdKM() {
        return idKM;
    }

    public void setIdKM(Integer idKM) {
        this.idKM = idKM;
    }

}
