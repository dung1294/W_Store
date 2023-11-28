
package Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.HoaDon;
import model.NhanVien;
import repository.JdbcHelper;


public class BillService {
    public void insert(HoaDon entity) {
        String sql = """
                     INSERT INTO [dbo].[HoaDon]
                                ([ngay_tao]
                                ,[tong_gia]
                                ,[trang_thai]
                                ,[tai_khoan_id]
                                ,[khach_hang_id]
                                ,[KMCT_id])
                          VALUES(?, ?, ?, ?, ?, ?)
                     """;
        JdbcHelper.update(sql,
                entity.getNgayTao(),
                entity.getTongGia(),
                entity.getTrangThai(),
                entity.getIdNV(),
                entity.getIdKH(),
                entity.getIdKM());
    }

    public void update(HoaDon entity) {
        String sql = """
                     UPDATE [dbo].[HoaDon]
                        SET [tong_gia] = ?
                           ,[trang_thai] = ?
                           ,[khach_hang_id] = ?
                      WHERE ID = ?
                     """;
        JdbcHelper.update(sql,
                entity.getTongGia(),
                entity.getTrangThai(),
                entity.getIdKH(),
                entity.getId());
    }

    public HoaDon selectById(Integer id) {
        String sql = """
                     SELECT 
                         hd.ID, 
                         hd.ma_hoa_don As Ma, 
                         nv.ten AS TenNV,
                         hd.ngay_tao AS NgayTao, 
                         hd.tong_gia AS TongTien, 
                         hd.trang_thai AS TrangThai
                     FROM 
                         dbo.HoaDon hd
                     JOIN 
                         dbo.TaiKhoan nv ON hd.tai_khoan_id = nv.ID
                     WHERE hd.ID = ?
                     """;
        List<HoaDon> list = this.selectBySql(sql, id);

        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    public HoaDon selectByMa(String ma) {
        String sql = """
                     SELECT 
                            hd.ID, 
                            hd.ma_hoa_don AS Ma, 
                            nv.ten AS TenNV,
                            hd.ngay_tao AS NgayTao, 
                            hd.tong_gia AS TongTien, 
                            hd.trang_thai AS TrangThai
                        FROM 
                            dbo.HoaDon hd
                        JOIN 
                            dbo.TaiKhoan nv ON hd.tai_khoan_id = nv.ID
                     WHERE hd.ma_hoa_don LIKE ?
                     """;
        List<HoaDon> list = this.selectBySql(sql, "%" + ma + "%");

        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    public List<HoaDon> selectAll() {
        String sql = """
                     SELECT 
                            hd.ID, 
                            hd.ma_hoa_don As Ma, 
                            nv.ten AS TenNV,
                            hd.ngay_tao AS NgayTao, 
                            hd.tong_gia AS TongTien, 
                            hd.trang_thai AS TrangThai
                        FROM 
                            dbo.HoaDon hd
                        JOIN 
                            dbo.TaiKhoan nv ON hd.tai_khoan_id = nv.ID
                     """;
        return this.selectBySql(sql);
    }

    protected List<HoaDon> selectBySql(String sql, Object... args) {
        List<HoaDon> list = new ArrayList<>();

        try {
            ResultSet rs = JdbcHelper.query(sql, args);
            while (rs.next()) {
                HoaDon hd = new HoaDon();

                hd.setId(rs.getInt("ID"));
                hd.setMa(rs.getString("Ma"));
                hd.setNgayTao(rs.getDate("NgayTao"));
                hd.setTongGia(rs.getDouble("TongTien"));
                hd.setTrangThai(rs.getInt("TrangThai"));
                hd.setNv(new NhanVien(rs.getString("TenNV")));

                list.add(hd);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public List<HoaDon> selectByStatus() {
        String sql = """
                     SELECT 
                            hd.ID, 
                            hd.ma_hoa_don AS Ma, 
                            nv.ten AS TenNV,
                            hd.ngay_tao AS NgayTao, 
                            hd.tong_gia AS TongTien, 
                            hd.trang_thai AS TrangThai
                        FROM 
                            dbo.HoaDon hd
                        JOIN 
                            dbo.TaiKhoan nv ON hd.tai_khoan_id = nv.ID
                     WHERE hd.trang_thai = 2
                     """;
        return this.selectBySql(sql);
    }
    
}
