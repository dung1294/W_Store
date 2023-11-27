package Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.NhanVien;
import model.Voucher;
import repository.JdbcHelper;

public class VoucherService {

    public void insert(Voucher entity) {
        String sql = """
                        INSERT INTO [dbo].[KhuyenMai]
                                   ([ma_khuyen_mai]
                                   ,[ten]
                                   ,[mo_ta]
                                   ,[ngay_tao]
                                   ,[tai_khoan_id])
                             VALUES (?, ?, ?, ?, ?)
                        """;

        JdbcHelper.update(sql,
                entity.getMa(),
                entity.getTen(),
                entity.getMoTa(),
                entity.getNgayTao(),
                entity.getIdNV());
    }

    public void update(Voucher entity) {
        String sql = """
                        UPDATE [dbo].[KhuyenMai]
                           SET [ten] = ?
                              ,[mo_ta] = ?
                              ,[ngay_tao] = ?
                              ,[tai_khoan_id] = ?
                         WHERE ID = ?
                        """;

        JdbcHelper.update(sql,
                entity.getTen(),
                entity.getMoTa(),
                entity.getNgayTao(),
                entity.getIdNV(),
                entity.getId());
    }

    public void delete(Integer id) {
        String delete_sql = """
                        DELETE FROM [dbo].[KhuyenMai]
                              WHERE ID = ?
                        """;

        JdbcHelper.update(delete_sql, id);
    }

    public Voucher selectById(Integer id) {
        String selectById = """
                        SELECT dbo.KhuyenMai.ID, 
                               dbo.KhuyenMai.ma_khuyen_mai, 
                               dbo.KhuyenMai.ten, 
                               dbo.KhuyenMai.mo_ta, 
                               dbo.KhuyenMai.ngay_tao, 
                               dbo.Taikhoan.ten AS TenNV
                        FROM dbo.KhuyenMai 
                        INNER JOIN dbo.Taikhoan ON dbo.KhuyenMai.tai_khoan_id = dbo.Taikhoan.ID
                         WHERE  dbo.KhuyenMai.ID = ?
                        """;
        List<Voucher> list = this.selectBySql(selectById, id);
        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    protected List<Voucher> selectBySql(String sql, Object... args) {
        List<Voucher> list = new ArrayList<>();
        try {
            ResultSet rs = JdbcHelper.query(sql, args);
            while (rs.next()) {
                Voucher vc = new Voucher();
                vc.setId(rs.getInt("ID"));
                vc.setMa(rs.getString("ma_khuyen_mai"));
                vc.setTen(rs.getString("ten"));
                vc.setMoTa(rs.getString("mo_ta"));
                vc.setNgayTao(rs.getDate("ngay_tao"));
                vc.setNv(new NhanVien(rs.getString("TenNV")));
                list.add(vc);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Voucher> selectAll() {
        String selectAll = """
                        SELECT dbo.KhuyenMai.ID, 
                                dbo.KhuyenMai.ma_khuyen_mai, 
                                dbo.KhuyenMai.ten, 
                                dbo.KhuyenMai.mo_ta, 
                                dbo.KhuyenMai.ngay_tao, 
                                dbo.Taikhoan.ten AS TenNV
                         FROM dbo.KhuyenMai 
                         INNER JOIN dbo.Taikhoan ON dbo.KhuyenMai.tai_khoan_id = dbo.Taikhoan.ID
                       """;
        return this.selectBySql(selectAll);
    }

    public Voucher selectByMa(String ma) {
        String sql = """
                        SELECT dbo.KhuyenMai.ID, 
                                dbo.KhuyenMai.ma_khuyen_mai, 
                                dbo.KhuyenMai.ten, 
                                dbo.KhuyenMai.mo_ta, 
                                dbo.KhuyenMai.ngay_tao, 
                                dbo.Taikhoan.ten AS TenNV
                         FROM dbo.KhuyenMai 
                         INNER JOIN dbo.Taikhoan ON dbo.KhuyenMai.tai_khoan_id = dbo.Taikhoan.ID
                        WHERE  dbo.KhuyenMai.ma_khuyen_mai LIKE ?
                   """;
        List<Voucher> list = this.selectBySql(sql, "%" + ma + "%");
        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    public List<Voucher> selectByKeyWord(String keyword) {
        String sql = """
                    SELECT dbo.KhuyenMai.ID, 
                            dbo.KhuyenMai.ma_khuyen_mai, 
                            dbo.KhuyenMai.ten, 
                            dbo.KhuyenMai.mo_ta, 
                            dbo.KhuyenMai.ngay_tao, 
                            dbo.Taikhoan.ten AS TenNV
                     FROM dbo.KhuyenMai 
                     INNER JOIN dbo.Taikhoan ON dbo.KhuyenMai.tai_khoan_id = dbo.Taikhoan.ID
                    WHERE dbo.KhuyenMai.ten like ? 
                            OR dbo.KhuyenMai.ma_khuyen_mai LIKE ?
                            OR dbo.Taikhoan.ten like ?
                     """;
        return this.selectBySql(sql,
                "%" + keyword + "%%",
                "%" + keyword + "%%",
                "%" + keyword + "%%");
    }

    public List<Voucher> searchKeyWord(String keyWord, int pages, int limit) {
        String sql = """
                     SELECT * 
                     FROM 
                     (
                        SELECT dbo.KhuyenMai.ID, 
                                dbo.KhuyenMai.ma_khuyen_mai, 
                                dbo.KhuyenMai.ten, 
                                dbo.KhuyenMai.mo_ta, 
                                dbo.KhuyenMai.ngay_tao, 
                                dbo.Taikhoan.ten AS TenNV
                         FROM dbo.KhuyenMai 
                         INNER JOIN dbo.Taikhoan ON dbo.KhuyenMai.tai_khoan_id = dbo.Taikhoan.ID
                        WHERE dbo.KhuyenMai.ten like ? 
                                OR dbo.KhuyenMai.ma_khuyen_mai LIKE ?
                                OR dbo.Taikhoan.ten like ?
                     ) AS FilteredResults
                     ORDER BY ID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;
        return this.selectBySql(sql,
                "%" + keyWord + "%%",
                "%" + keyWord + "%%",
                "%" + keyWord + "%%",
                (pages - 1) * limit, limit);
    }

    public List<Voucher> selectPagesFilter(String keyword, Integer nam, Integer thang) {
        String sql = """
                            SELECT dbo.KhuyenMai.ID, 
                                    dbo.KhuyenMai.ma_khuyen_mai, 
                                    dbo.KhuyenMai.ten, 
                                    dbo.KhuyenMai.mo_ta, 
                                    dbo.KhuyenMai.ngay_tao, 
                                    dbo.Taikhoan.ten AS TenNV
                             FROM dbo.KhuyenMai 
                             INNER JOIN dbo.Taikhoan ON dbo.KhuyenMai.tai_khoan_id = dbo.Taikhoan.ID
                            WHERE (dbo.KhuyenMai.ten like ? OR dbo.KhuyenMai.ma_khuyen_mai LIKE ?)
                                    and ? Between year(dbo.KhuyenMai.ngay_tao) and year(dbo.KhuyenMai.ngay_tao)
                                    and ? between Month(dbo.KhuyenMai.ngay_tao) and month(dbo.KhuyenMai.ngay_tao)
                     
                     """;
        return this.selectBySql(sql,
                "%" + keyword + "%%",
                "%" + keyword + "%%",
                nam,
                thang);
    }

    public List<Voucher> Filter(String keyword, Integer nam, Integer thang, int pages, int limit) {
        String sql = """
                     SELECT * 
                     FROM 
                     (
                         SELECT dbo.KhuyenMai.ID, 
                                dbo.KhuyenMai.ma_khuyen_mai, 
                                dbo.KhuyenMai.ten, 
                                dbo.KhuyenMai.mo_ta, 
                                dbo.KhuyenMai.ngay_tao, 
                                dbo.Taikhoan.ten AS TenNV
                         FROM dbo.KhuyenMai 
                         INNER JOIN dbo.Taikhoan ON dbo.KhuyenMai.tai_khoan_id = dbo.Taikhoan.ID
                        WHERE (dbo.KhuyenMai.ten like ? OR dbo.KhuyenMai.ma_khuyen_mai LIKE ?)
                                and ? Between year(dbo.KhuyenMai.ngay_tao) and year(dbo.KhuyenMai.ngay_tao)
                                and ? between Month(dbo.KhuyenMai.ngay_tao) and month(dbo.KhuyenMai.ngay_tao)
                     ) AS FilteredResults
                     ORDER BY ID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;
        return this.selectBySql(sql,
                "%" + keyword + "%%",
                "%" + keyword + "%%",
                nam,
                thang,
                (pages - 1) * limit, limit);
    }
}
