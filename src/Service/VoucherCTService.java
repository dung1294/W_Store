package Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Voucher;
import model.VoucherCT;
import repository.JdbcHelper;

public class VoucherCTService {

    public void insert(VoucherCT entity) {
        String sql = """
                     INSERT INTO [dbo].[KMCT]
                                ([ngay_bat_dau]
                                ,[ngay_ket_thuc]
                                ,[so_luong]
                                ,[kieu_giam]
                                ,[trang_thai]
                                ,[khuyen_mai_id])
                          VALUES(?, ?, ?, ?, ?, ?)
                     """;
        JdbcHelper.update(sql,
                entity.getNgBatDau(),
                entity.getNgKetThuc(),
                entity.getSoLuong(),
                entity.getKieuGiam(),
                entity.getTrangThai(),
                entity.getIdKM());
    }

    public void update(VoucherCT entity) {
        String sql = """
                     UPDATE [dbo].[KMCT]
                        SET [ngay_bat_dau] = ?
                           ,[ngay_ket_thuc] = ?
                           ,[so_luong] = ?
                           ,[kieu_giam] = ?
                           ,[trang_thai] = ?
                           ,[khuyen_mai_id] = ?
                      WHERE ID = ?
                     """;
        JdbcHelper.update(sql,
                entity.getNgBatDau(),
                entity.getNgKetThuc(),
                entity.getSoLuong(),
                entity.getKieuGiam(),
                entity.getTrangThai(),
                entity.getIdKM(),
                entity.getId());
    }

    public void delete(Integer id) {
        String sql = """
                     delete from KMCT
                     where ID = ?
                     """;
        JdbcHelper.update(sql, id);
    }

    protected List<VoucherCT> selectBySql(String sql, Object... args) {
        List<VoucherCT> list = new ArrayList<>();

        try {
            ResultSet rs = JdbcHelper.query(sql, args);
            while (rs.next()) {
                VoucherCT vcct = new VoucherCT();
                vcct.setId(rs.getInt("ID"));
                vcct.setSoLuong(rs.getInt("SoLuong"));
                vcct.setTrangThai(rs.getInt("TrangThai"));
                vcct.setKieuGiam(rs.getInt("KieuGiam"));
                vcct.setNgBatDau(rs.getDate("NgBatDau"));
                vcct.setNgKetThuc(rs.getDate("NgKetThuc"));
                vcct.setVc(new Voucher(rs.getString("MaVC"),
                        rs.getString("TenVC")
                ));

                list.add(vcct);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public VoucherCT selectById(Integer id) {
        String sql = """
                    SELECT dbo.KMCT.ID, 
                               dbo.KhuyenMai.ma_khuyen_mai AS MaVC, 
                               dbo.KhuyenMai.ten AS TenVC, 
                               dbo.KMCT.ngay_bat_dau AS NgBatDau, 
                               dbo.KMCT.ngay_ket_thuc AS NgKetThuc, 
                               dbo.KMCT.so_luong AS SoLuong, 
                               dbo.KMCT.kieu_giam AS KieuGiam, 
                               dbo.KMCT.trang_thai AS TrangThai
                        FROM dbo.KhuyenMai 
                        INNER JOIN dbo.KMCT ON dbo.KhuyenMai.ID = dbo.KMCT.khuyen_mai_id
                     WHERE dbo.KMCT.ID = ?
                     """;
        List<VoucherCT> list = this.selectBySql(sql, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public List<VoucherCT> selectByMa(String ma) {
        String sql = """
                     SELECT dbo.KMCT.ID, 
                        dbo.KhuyenMai.ma_khuyen_mai AS MaVC, 
                        dbo.KhuyenMai.ten AS TenVC, 
                        dbo.KMCT.ngay_bat_dau AS NgBatDau, 
                        dbo.KMCT.ngay_ket_thuc AS NgKetThuc, 
                        dbo.KMCT.so_luong AS SoLuong, 
                        dbo.KMCT.kieu_giam AS KieuGiam, 
                        dbo.KMCT.trang_thai AS TrangThai
                     FROM dbo.KhuyenMai 
                     INNER JOIN dbo.KMCT ON dbo.KhuyenMai.ID = dbo.KMCT.khuyen_mai_id
                     WHERE dbo.KhuyenMai.ma_khuyen_mai like ?
                     """;
        return this.selectBySql(sql, "%" + ma + "%");
    }

    public List<VoucherCT> selectAll() {
        String sql = """
                    SELECT dbo.KMCT.ID, 
                        dbo.KhuyenMai.ma_khuyen_mai AS MaVC, 
                        dbo.KhuyenMai.ten AS TenVC, 
                        dbo.KMCT.ngay_bat_dau AS NgBatDau, 
                        dbo.KMCT.ngay_ket_thuc AS NgKetThuc, 
                        dbo.KMCT.so_luong AS SoLuong, 
                        dbo.KMCT.kieu_giam AS KieuGiam, 
                        dbo.KMCT.trang_thai AS TrangThai
                     FROM dbo.KhuyenMai 
                     INNER JOIN dbo.KMCT ON dbo.KhuyenMai.ID = dbo.KMCT.khuyen_mai_id
                     """;
        return this.selectBySql(sql);
    }

    public List<VoucherCT> selectByKeyWord(String maVC) {
        String sql = """
                            SELECT dbo.KMCT.ID, 
                                dbo.KhuyenMai.ma_khuyen_mai AS MaVC, 
                                dbo.KhuyenMai.ten AS TenVC, 
                                dbo.KMCT.ngay_bat_dau AS NgBatDau, 
                                dbo.KMCT.ngay_ket_thuc AS NgKetThuc, 
                                dbo.KMCT.so_luong AS SoLuong, 
                                dbo.KMCT.kieu_giam AS KieuGiam, 
                                dbo.KMCT.trang_thai AS TrangThai
                             FROM dbo.KhuyenMai 
                             INNER JOIN dbo.KMCT ON dbo.KhuyenMai.ID = dbo.KMCT.khuyen_mai_id
                     WHERE dbo.KhuyenMai.ma_khuyen_mai like ?
                     """;
        return this.selectBySql(sql,
                "%" + maVC + "%");
    }

    public List<VoucherCT> searchKeyWord(String maVC, int pages, int limit) {
        String sql = """
                     SELECT * 
                     FROM 
                     (
                         SELECT dbo.KMCT.ID, 
                            dbo.KhuyenMai.ma_khuyen_mai AS MaVC, 
                            dbo.KhuyenMai.ten AS TenVC, 
                            dbo.KMCT.ngay_bat_dau AS NgBatDau, 
                            dbo.KMCT.ngay_ket_thuc AS NgKetThuc, 
                            dbo.KMCT.so_luong AS SoLuong, 
                            dbo.KMCT.kieu_giam AS KieuGiam, 
                            dbo.KMCT.trang_thai AS TrangThai
                         FROM dbo.KhuyenMai 
                            INNER JOIN dbo.KMCT ON dbo.KhuyenMai.ID = dbo.KMCT.khuyen_mai_id
                         WHERE dbo.KhuyenMai.ma_khuyen_mai like ?
                     ) AS FilteredResults
                     ORDER BY ID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;
        return this.selectBySql(sql,
                "%" + maVC + "%",
                (pages - 1) * limit, limit);
    }

    public List<VoucherCT> FilterPageByMa(String maVC, Integer trangThai, Integer kieuGiam, Integer thang) {
        String sql = """
                            SELECT dbo.KMCT.ID, 
                                dbo.KhuyenMai.ma_khuyen_mai AS MaVC, 
                                dbo.KhuyenMai.ten AS TenVC, 
                                dbo.KMCT.ngay_bat_dau AS NgBatDau, 
                                dbo.KMCT.ngay_ket_thuc AS NgKetThuc, 
                                dbo.KMCT.so_luong AS SoLuong, 
                                dbo.KMCT.kieu_giam AS KieuGiam, 
                                dbo.KMCT.trang_thai AS TrangThai
                             FROM dbo.KhuyenMai 
                             INNER JOIN dbo.KMCT ON dbo.KhuyenMai.ID = dbo.KMCT.khuyen_mai_id
                     WHERE dbo.KhuyenMai.ma_khuyen_mai like ?
                           and dbo.KMCT.trang_thai = ?
                           and dbo.KMCT.kieu_giam = ?
                           and month(dbo.KMCT.ngay_bat_dau) = ?
                     """;
        return this.selectBySql(sql,
                "%" + maVC + "%",
                trangThai,
                kieuGiam,
                thang);
    }

    public List<VoucherCT> FilterDataByMa(String maVC, Integer trangThai, Integer kieuGiam, Integer thang, int pages, int limit) {
        String sql = """
                     SELECT * 
                     FROM 
                     (
                         SELECT dbo.KMCT.ID, 
                            dbo.KhuyenMai.ma_khuyen_mai AS MaVC, 
                            dbo.KhuyenMai.ten AS TenVC, 
                            dbo.KMCT.ngay_bat_dau AS NgBatDau, 
                            dbo.KMCT.ngay_ket_thuc AS NgKetThuc, 
                            dbo.KMCT.so_luong AS SoLuong, 
                            dbo.KMCT.kieu_giam AS KieuGiam, 
                            dbo.KMCT.trang_thai AS TrangThai
                         FROM dbo.KhuyenMai 
                            INNER JOIN dbo.KMCT ON dbo.KhuyenMai.ID = dbo.KMCT.khuyen_mai_id
                         WHERE dbo.KhuyenMai.ma_khuyen_mai like ?
                                and dbo.KMCT.trang_thai = ?
                                and dbo.KMCT.kieu_giam = ?
                                and month(dbo.KMCT.ngay_bat_dau) = ?
                     ) AS FilteredResults
                     ORDER BY ID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;
        return this.selectBySql(sql,
                "%" + maVC + "%",
                trangThai,
                kieuGiam,
                thang,
                (pages - 1) * limit, limit);
    }
}
