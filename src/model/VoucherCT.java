package model;

import java.sql.Date;

public class VoucherCT {

    private Integer id;
    private Integer idKM;
    private Date ngBatDau;
    private Date ngKetThuc;
    private Integer soLuong;
    private Integer kieuGiam;
    private Integer trangThai;
    private Voucher vc;

    public VoucherCT() {
    }

    public VoucherCT(Integer id, Integer idKM, Date ngBatDau, Date ngKetThuc, Integer soLuong, Integer kieuGiam, Integer trangThai, Voucher vc) {
        this.id = id;
        this.idKM = idKM;
        this.ngBatDau = ngBatDau;
        this.ngKetThuc = ngKetThuc;
        this.soLuong = soLuong;
        this.kieuGiam = kieuGiam;
        this.trangThai = trangThai;
        this.vc = vc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdKM() {
        return idKM;
    }

    public void setIdKM(Integer idKM) {
        this.idKM = idKM;
    }

    public Date getNgBatDau() {
        return ngBatDau;
    }

    public void setNgBatDau(Date ngBatDau) {
        this.ngBatDau = ngBatDau;
    }

    public Date getNgKetThuc() {
        return ngKetThuc;
    }

    public void setNgKetThuc(Date ngKetThuc) {
        this.ngKetThuc = ngKetThuc;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Integer getKieuGiam() {
        return kieuGiam;
    }

    public void setKieuGiam(Integer kieuGiam) {
        this.kieuGiam = kieuGiam;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    public Voucher getVc() {
        return vc;
    }

    public void setVc(Voucher vc) {
        this.vc = vc;
    }

    public String loadTrangThai() {
        if (trangThai == 1) {
            return "Có thể dùng";
        }
        return "Không thể dùng";
    }

    public String loadKieuGiam() {
        if (kieuGiam == 1) {
            return "VNĐ";
        }
        return "%";
    }
}
