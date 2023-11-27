package Service;

import repository.JdbcHelper;
import java.util.ArrayList;
import java.util.List;
import model.SanPhamCT;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.HinhAnh;
import model.HinhDang;
import model.MauSac;
import model.SanPham;

public class SanPhamCTService {

    public void insert(SanPhamCT entity) {
        String sql = """
                    INSERT INTO [dbo].[SPCT]
                               ([gia]
                               ,[so_luong]
                               ,[trang_thai]
                               ,[san_pham_id]
                               ,[mau_sac_id]
                               ,[hinh_dang_id]
                               ,[anh_id])
                          VALUES (?, ?, ?, ?, ?, ?, ?)
                     """;

        JdbcHelper.update(sql,
                entity.getGia(),
                entity.getSoLuong(),
                entity.getTrangThai(),
                entity.getId_sanPham(),
                entity.getId_mauSac(),
                entity.getId_HinhDang(),
                entity.getId_Anh());
    }

    public void update(SanPhamCT entity) {
        String sql = """
                     UPDATE [dbo].[SPCT]
                        SET [Gia] = ?
                           ,[so_luong] = ?
                           ,[trang_thai] = ?
                           ,[san_pham_id] = ?
                           ,[mau_sac_id] = ?
                           ,[hinh_dang_id] = ?
                           ,[anh_id] = ?
                      WHERE ID = ?
                     """;

        JdbcHelper.update(sql,
                entity.getGia(),
                entity.getSoLuong(),
                entity.getTrangThai(),
                entity.getId_sanPham(),
                entity.getId_mauSac(),
                entity.getId_HinhDang(),
                entity.getId_Anh(),
                entity.getId()
        );
    }

    public void updateSoLuong(SanPhamCT entity) {
        String sql = """
                     UPDATE [dbo].[SPCT]
                        SET [so_luong] = ?
                            ,[trang_thai] = ?
                      WHERE ID = ?
                     """;

        JdbcHelper.update(sql,
                entity.getSoLuong(),
                entity.getTrangThai(),
                entity.getId()
        );
    }

    public SanPhamCT selectById(Integer id) {
        String sql = """
                    SELECT 
                            spct.ID, 
                            sp.ma_san_pham,
                            sp.ten, 
                            spct.gia, 
                            spct.so_luong,
                            ms.ten_mau as MauSac, 
                            hd.kieu_dang as HinhDang, 
                            anh.link as HinhAnh, 
                            spct.trang_thai
                        FROM dbo.SPCT spct
                        INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                        INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                        INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                        INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                     WHERE spct.ID = ?
                     """;
        List<SanPhamCT> list = this.selectBySql(sql, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public List<SanPhamCT> selectByMa(String ma) {
        String sql = """
                    SELECT 
                            spct.ID, 
                            sp.ma_san_pham,
                            sp.ten, 
                            spct.gia, 
                            spct.so_luong,
                            ms.ten_mau as MauSac, 
                            hd.kieu_dang as HinhDang, 
                            anh.link as HinhAnh, 
                            spct.trang_thai
                        FROM dbo.SPCT spct
                        INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                        INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                        INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                        INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                     WHERE sp.ma_san_pham like ?
                     """;
        return this.selectBySql(sql, "%" + ma + "%");
    }

    public List<SanPhamCT> selectAll() {
        String sql = """
                    SELECT 
                            spct.ID, 
                            sp.ma_san_pham,
                            sp.ten, 
                            spct.gia, 
                            spct.so_luong,
                            ms.ten_mau as MauSac, 
                            hd.kieu_dang as HinhDang, 
                            anh.link as HinhAnh, 
                            spct.trang_thai
                        FROM dbo.SPCT spct
                        INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                        INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                        INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                        INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                     """;
        return this.selectBySql(sql);
    }

    protected List<SanPhamCT> selectBySql(String sql, Object... args) {
        List<SanPhamCT> list = new ArrayList<>();

        try {
            ResultSet rs = JdbcHelper.query(sql, args);
            while (rs.next()) {
                SanPhamCT spct = new SanPhamCT();
                spct.setId(rs.getInt("ID"));
                spct.setGia(rs.getDouble("gia"));
                spct.setSoLuong(rs.getInt("so_luong"));
                spct.setTrangThai(rs.getInt("trang_thai"));
                spct.setSanPham(new SanPham(rs.getString("ma_san_pham"),
                        rs.getString("ten")
                ));
                spct.setHinhDang(new HinhDang(rs.getString("HinhDang")));
                spct.setMauSac(new MauSac(rs.getString("MauSac")));
                spct.setHinhAnh(new HinhAnh(rs.getString("HinhAnh")));

                list.add(spct);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public List<SanPhamCT> selectByKeyWord(String maSP) {
        String sql = """
                            SELECT 
                                spct.ID, 
                                sp.ma_san_pham,
                                sp.ten, 
                                spct.gia, 
                                spct.so_luong,
                                ms.ten_mau as MauSac, 
                                hd.kieu_dang as HinhDang, 
                                anh.link as HinhAnh, 
                                spct.trang_thai
                            FROM dbo.SPCT spct
                            INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                            INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                            INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                            INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                     WHERE sp.ma_san_pham like ?
                     """;
        return this.selectBySql(sql,
                "%" + maSP + "%");
    }

    public List<SanPhamCT> searchKeyWord(String maSP, int pages, int limit) {
        String sql = """
                     SELECT * 
                     FROM 
                     (
                         SELECT 
                            spct.ID, 
                            sp.ma_san_pham,
                            sp.ten, 
                            spct.gia, 
                            spct.so_luong,
                            ms.ten_mau as MauSac, 
                            hd.kieu_dang as HinhDang, 
                            anh.link as HinhAnh, 
                            spct.trang_thai
                        FROM dbo.SPCT spct
                        INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                        INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                        INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                        INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                     WHERE sp.ma_san_pham like ?
                     ) AS FilteredResults
                     ORDER BY ID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;
        return this.selectBySql(sql,
                "%" + maSP + "%",
                (pages - 1) * limit, limit);
    }

    public List<SanPhamCT> selectPageStatus(String keyWord) {
        String sql = """
                            SELECT 
                                spct.ID, 
                                sp.ma_san_pham,
                                sp.ten, 
                                spct.gia, 
                                spct.so_luong,
                                ms.ten_mau as MauSac, 
                                hd.kieu_dang as HinhDang, 
                                anh.link as HinhAnh, 
                                spct.trang_thai
                            FROM dbo.SPCT spct
                            INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                            INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                            INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                            INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                     WHERE spct.trang_thai = 1
                           and (sp.ma_san_pham like ? or sp.ten like ?)
                     """;
        return this.selectBySql(sql,
                "%" + keyWord + "%%",
                "%" + keyWord + "%%");
    }

    public List<SanPhamCT> searchKeyWordStatus(String keyWord, int pages, int limit) {
        String sql = """
                     SELECT * 
                     FROM 
                     (
                         SELECT 
                            spct.ID, 
                            sp.ma_san_pham,
                            sp.ten, 
                            spct.gia, 
                            spct.so_luong,
                            ms.ten_mau as MauSac, 
                            hd.kieu_dang as HinhDang, 
                            anh.link as HinhAnh, 
                            spct.trang_thai
                        FROM dbo.SPCT spct
                        INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                        INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                        INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                        INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                     WHERE spct.trang_thai = 1
                           and (sp.ma_san_pham like ? or sp.ten like ?)
                     ) AS FilteredResults
                     ORDER BY ID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;
        return this.selectBySql(sql,
                "%" + keyWord + "%",
                "%" + keyWord + "%",
                (pages - 1) * limit, limit);
    }

    public List<SanPhamCT> FilterPageByMa(Double giaMin, Double giaMax, String mau, String hinhDang, String maSP) {
        String sql = """
                          SELECT 
                                spct.ID, 
                                sp.ma_san_pham,
                                sp.ten, 
                                spct.gia, 
                                spct.so_luong,
                                ms.ten_mau as MauSac, 
                                hd.kieu_dang as HinhDang, 
                                anh.link as HinhAnh, 
                                spct.trang_thai
                            FROM dbo.SPCT spct
                            INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                            INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                            INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                            INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                         WHERE 
                              (spct.gia BETWEEN COALESCE(?, spct.gia) AND COALESCE(?, spct.gia))
                              and ms.ten_mau like ?
                              and hd.kieu_dang like ?
                              and sp.ma_san_pham like ?
                     """;
        return this.selectBySql(sql,
                giaMin, giaMax,
                "%" + mau + "%",
                "%" + hinhDang + "%",
                "%" + maSP + "%");
    }

    public List<SanPhamCT> FilterDataByMa(Double giaMin, Double giaMax, String mau, String hinhDang, String maSP, int pages, int limit) {
        String sql = """
                     SELECT * 
                     FROM 
                     (
                        SELECT 
                                spct.ID, 
                                sp.ma_san_pham,
                                sp.ten, 
                                spct.gia, 
                                spct.so_luong,
                                ms.ten_mau as MauSac, 
                                hd.kieu_dang as HinhDang, 
                                anh.link as HinhAnh, 
                                spct.trang_thai
                            FROM dbo.SPCT spct
                            INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                            INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                            INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                            INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                         WHERE 
                              spct.gia BETWEEN ISNULL (?, spct.gia) AND  ISNULL (?, spct.gia)
                              and ms.ten_mau like ?
                              and hd.kieu_dang like ?
                              and sp.ma_san_pham like ?
                     ) AS FilteredResults
                     ORDER BY ID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;
        return this.selectBySql(sql,
                giaMin, giaMax,
                "%" + mau + "%",
                "%" + hinhDang + "%",
                "%" + maSP + "%",
                (pages - 1) * limit, limit);
    }

    public List<SanPhamCT> FilterPage(String keyWord, Double giaMin, Double giaMax, String mau, String hinhDang) {
        String sql = """
                          SELECT 
                                spct.ID, 
                                sp.ma_san_pham,
                                sp.ten, 
                                spct.gia, 
                                spct.so_luong,
                                ms.ten_mau as MauSac, 
                                hd.kieu_dang as HinhDang, 
                                anh.link as HinhAnh, 
                                spct.trang_thai
                            FROM dbo.SPCT spct
                            INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                            INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                            INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                            INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                         WHERE (sp.ma_san_pham like ? or sp.ten like ?)
                              and spct.gia BETWEEN ISNULL (?, spct.gia) AND  ISNULL (?, spct.gia)
                              and ms.ten_mau like ?
                              and hd.kieu_dang like ?
                              and spct.trang_thai = 1
                     """;
        return this.selectBySql(sql,
                "%" + keyWord + "%%",
                "%" + keyWord + "%%",
                giaMin, giaMax,
                "%" + mau + "%",
                "%" + hinhDang + "%");
    }

    public List<SanPhamCT> FilterData(String keyWord, Double giaMin, Double giaMax, String mau, String hinhDang, int pages, int limit) {
        String sql = """
                     SELECT * 
                     FROM 
                     (
                        SELECT 
                                spct.ID, 
                                sp.ma_san_pham,
                                sp.ten, 
                                spct.gia, 
                                spct.so_luong,
                                ms.ten_mau as MauSac, 
                                hd.kieu_dang as HinhDang, 
                                anh.link as HinhAnh, 
                                spct.trang_thai
                            FROM dbo.SPCT spct
                            INNER JOIN dbo.SanPham sp ON spct.san_pham_id = sp.ID
                            INNER JOIN dbo.MauSac ms ON spct.mau_sac_id = ms.ID
                            INNER JOIN dbo.HinhDang hd ON spct.hinh_dang_id = hd.ID
                            INNER JOIN dbo.Anh anh ON spct.anh_id = anh.ID
                         WHERE (sp.ma_san_pham like ? or sp.ten like ?)
                              and spct.gia BETWEEN ISNULL (?, spct.gia) AND  ISNULL (?, spct.gia)
                              and ms.ten_mau like ?
                              and hd.kieu_dang like ?
                              and spct.trang_thai = 1
                     ) AS FilteredResults
                     ORDER BY ID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;
        return this.selectBySql(sql,
                "%" + keyWord + "%%",
                "%" + keyWord + "%%",
                giaMin, giaMax,
                "%" + mau + "%",
                "%" + hinhDang + "%",
                (pages - 1) * limit, limit);
    }
}
