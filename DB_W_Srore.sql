Create database Da_1_Thi
GO

USE Da_1_Thi
GO


CREATE TABLE DanhMuc(
ID int IDENTITY (1,1) PRIMARY KEY,
ten_danh_muc nvarchar(255) not null
)
Go

CREATE TABLE ThuongHieu(
ID int IDENTITY (1,1) PRIMARY KEY,
ten_thuong_hieu nvarchar(255) not null
)
Go

CREATE TABLE PhanLoai(
ID int IDENTITY (1,1) PRIMARY KEY,
phan_loai nvarchar(255) not null
)
Go

CREATE TABLE ChatLieu(
ID int IDENTITY (1,1) PRIMARY KEY,
chat_lieu nvarchar(255) not null
)
GO


CREATE TABLE MauSac(
ID int IDENTITY (1,1) PRIMARY KEY,
ten_mau nvarchar(255) not null
)
Go

CREATE TABLE HinhDang(
ID int IDENTITY (1,1) PRIMARY KEY,
kieu_dang nvarchar(255) not null,
)
Go

CREATE TABLE Anh(
ID int IDENTITY (1,1) PRIMARY KEY,
link varchar(255)
)
Go


CREATE TABLE SanPham(
ID int IDENTITY (1,1) PRIMARY KEY,
ma_san_pham varchar(255) UNIQUE,
ten nvarchar(255) not null,
mo_ta nvarchar(255),
ngay_tao DATETIME DEFAULT GETDATE(),
ngay_sua DATETIME DEFAULT GETDATE(),
danh_muc_id int foreign key (danh_muc_id) references DanhMuc(ID),
thuong_hieu_id int foreign key (thuong_hieu_id) references ThuongHieu(ID),
phan_loai_id int foreign key (phan_loai_id) references PhanLoai(ID),
chat_lieu_id int foreign key (chat_lieu_id) references ChatLieu(ID)
)
Go

CREATE TABLE SPCT(
ID int IDENTITY (1,1) PRIMARY KEY,
gia Float not null,
so_luong int not null,
trang_thai int not null,
san_pham_id int foreign key (san_pham_id) references SanPham(ID),
mau_sac_id int foreign key(mau_sac_id) references MauSac(ID),
hinh_dang_id int foreign key (hinh_dang_id) references HinhDang(ID),
anh_id int foreign key (anh_id) references Anh(ID)
)
Go

CREATE TABLE Taikhoan(
ID int IDENTITY (1,1) PRIMARY KEY,
tai_khoan varchar(255) not null UNIQUE,
mat_khau varchar(255) not null,
ten nvarchar(255) not null,
dia_chi nvarchar(255),
SDT varchar(255),
email varchar(255),
vai_tro int not null,
trang_thai int not null
)
Go

CREATE TABLE Voucher(
ID int IDENTITY (1,1) PRIMARY KEY,
ma varchar(255) not null UNIQUE,
ten nvarchar(255),
mo_ta nvarchar(255),
ngay_tao DATETIME DEFAULT GETDATE(),
ngay_bat_dau DATE not null,
ngay_ket_thuc DATE not null,
so_luong int,
kieu_giam int,
gia_tri float,
trang_thai int not null,
tai_khoan_id int foreign key (tai_khoan_id) references TaiKhoan(ID),
)
Go


CREATE TABLE KhachHang(
ID int IDENTITY (1,1) PRIMARY KEY,
ma_khach_hang varchar(255) UNIQUE,
ten varchar(255) not null,
SDT varchar(20) not null
)
Go

CREATE TABLE HoaDon(
ID int IDENTITY (1,1) PRIMARY KEY,
ma_hoa_don Varchar(10) UNIQUE,
ngay_tao DATETIME DEFAULT GETDATE(),
tong_gia Float,
thanhToan Float,
trang_thai int,
tai_khoan_id int foreign key (tai_khoan_id) references TaiKhoan(ID),
khach_hang_id int foreign key (khach_hang_id) references KhachHang(ID),
ID_Voucher int foreign key(ID_Voucher) references Voucher(ID)
)
GO

CREATE TABLE HDCT(
ID int IDENTITY (1,1) PRIMARY KEY,
hoa_don_id int foreign key(hoa_don_id) references HoaDon(ID),
SPCT_id int foreign key(SPCT_id) references SPCT(ID),
so_luong int not null,
gia Float
)
GO

INSERT INTO DANHMUC values 
(N'Gọng kính'),
(N'Tròng Kính'),
(N'Kính râm')
Go


INSERT INTO ThuongHieu values
('Chemi'),
('Elements'),
('Essilor'),
('Hoga')
Go

INSERT INTO PhanLoai values
(N'Kính cận'),
(N'Kính râm')
Go

INSERT INTO ChatLieu values 
(N'Nhựa'),
(N'Kim loại'),
(N'Nhựa mix kim loại')
Go

INSERT INTO MauSac
VALUES  (N'Đỏ'),
		(N'Trắng'),
		(N'Đen'),
		(N'Hồng')
Go

insert into HinhDang values 
(N'Vuông'), 
(N'Mắt mèo'), 
('oval'), 
('browline')
Go

insert into Anh values 
(N'image1.jpg'),
(N'image2.jpg'),
(N'image3.jpg'),
(N'image4.jpg'),
(N'image5.jpg')
Go

insert into SanPham (ma_san_pham, ten, mo_ta, danh_muc_id, thuong_hieu_id, phan_loai_id, chat_lieu_id) values 
('SP01', N'Kính báo hồng', '', 3, 2, 2, 2),
('SP02', N'Gọng kính cận', '', 1, 1, 1, 1),
('SP03', N'Tròng kính cận', '', 2, 1, 1, 1),
('SP04', N'Gọng kính râm', '', 1, 4, 1, 3),
('SP05', N'Kính râm thời trang', '', 3, 3, 2, 3),
('SP06', N'Kính mắt hồng', '', 3, 2, 1, 1),
('SP07', N'Gọng kính cận thời trang', '', 1, 1, 1, 2),
('SP08', N'Tròng kính mắt đen', '', 2, 2, 1, 1),
('SP09', N'Gọng kính râm hợp thời trang', '', 1, 4, 2, 3),
('SP10', N'Kính râm mắt mèo', '', 3, 3, 2, 2); 
Go


INSERT INTO [dbo].[SPCT]([gia],[so_luong],[trang_thai],[san_pham_id],[mau_sac_id],[hinh_dang_id],[anh_id])
VALUES  (50000, 25, 1, 1, 1, 2, 1),
		(230000, 30, 1, 2, 3, 3, 2),
		(320000, 20, 1, 3, 2, 1, 3),
		(140000, 12, 1, 4, 1, 4, 4),
		(230000, 8, 1, 5, 4, 2, 5),
		(150000, 15, 1, 6, 1, 2, 1),
		(180000, 10, 1, 7, 3, 3, 2),
		(120000, 20, 1, 8, 2, 1, 3),
		(200000, 12, 1, 9, 1, 4, 4),
		(250000, 8, 1, 10, 4, 2, 5);
Go

INSERT INTO [dbo].[Taikhoan]([tai_khoan], [mat_khau], [ten], [dia_chi], [SDT], [email], [vai_tro], [trang_thai])
     VALUES ('NV01', '1234', N'Nguyễn Văn Tèo', null, '0367439572',  N'teoph9081@gmai.com', 0, 0),
		 ('PH40152', '123456', N'Lê Đình Huy', null, '0367439572',  N'huyldph40152@fpt.edu.vn', 1, 0),
		 ('PH41866', '123456', N'Vũ Minh Hưng', null, '0372891516',  N'minhhung180104@gmail.com', 1, 0)
Go
	 
INSERT INTO [dbo].[KhachHang]([ma_khach_hang] ,[ten] ,[SDT])
     VALUES ('KH01', N'Lê Đình Huy', '0987654321'),
			('KH02', N'Nguyen Thi Lan', '0901234567'),
			('KH03', N'Tran Van An', '0987654321'),
			('KH04', N'Ngo Minh Thuan', '0912345678'),
			('KH05', N'Ha Thi Huong', '0978765432'),
			('KH06', N'Le Van Minh', '0923456789'),
			('KH07', N'Vũ Minh Hưng', '0372891516');
Go

INSERT INTO [dbo].[HoaDon]([ma_hoa_don], [ngay_tao], [tong_gia], [thanhToan], [trang_thai], [tai_khoan_id], [khach_hang_id], [ID_Voucher])
VALUES 
(null, '2022-03-11', 500000, 400000, 1, 1, 1, 1),
(null, '2022-03-11', 620000, 420000, 1, 2, 2, 3),
(null, '2022-04-20', 700000, 500000, 1, 1, 3, 3),
(null, '2024-11-09', 760000, 580000, 1, 2, 4, 5),
(null, '2024-10-08', 0, 0, 2, 1, 5, null),
(null, '2024-12-11', 400000, 0, 3, 2, 1, null),
(null, Default, 900000, 700000, 1, 1, 2, 9),
(null, Default, 600000, 500000, 1, 2, 3, 1),
(null, Default, 800000, 500000, 1, 1, 4, 7),
(null, Default, 100000, 0, 3, 2, 5, null);

Go

INSERT INTO [dbo].[HDCT]([hoa_don_id] ,[SPCT_id] ,[so_luong] ,[gia])
VALUES 
(1, 4, 4, 50000),
(1, 9, 3, 100000),
(2, 6, 1, 320000),
(2, 10, 1, 180000),
(2, 11, 1, 120000),
(3, 8, 1, 230000),
(3, 9, 2, 150000),
(3, 4, 1, 50000),
(3, 11, 1, 120000),
(4, 8, 1, 230000),
(4, 9, 2, 150000),
(4, 4, 1, 50000),
(4, 10, 1, 180000),
(6, 12, 2, 200000),
(7, 8, 1, 230000),
(7, 9, 2, 150000),
(7, 4, 1, 50000),
(7, 10, 1, 180000),
(7, 7, 1, 140000),
(8, 6, 1, 320000),
(8, 10, 1, 180000),
(8, 4, 2, 50000),
(9, 8, 1, 230000),
(9, 4, 5, 50000),
(9, 10, 1, 180000),
(9, 7, 1, 140000),
(10, 4, 2, 50000)
Go

INSERT INTO [dbo].[Voucher] ([ma] ,[ten] ,[mo_ta] ,[ngay_tao] ,[ngay_bat_dau] ,[ngay_ket_thuc] ,[so_luong] ,[kieu_giam] ,[gia_tri] ,[trang_thai] ,[tai_khoan_id])
VALUES 
('VC03142', N'Giảm giá đầu tháng', N'Áp dụng cho mọi đơn hàng', Default, '2023-12-01', '2023-12-10', 20, 1, 100000, 1, 1),
('VC03143', N'Khuyến mãi hấp dẫn', N'Áp dụng cho mọi đơn hàng', Default, '2023-12-05', '2023-12-15', 15, 2, 15, 1, 2),
('VC03144', N'Ưu đãi đặc biệt', N'Áp dụng cho mọi đơn hàng', Default, '2023-12-10', '2023-12-20', 10, 1, 200000, 1, 1),
('VC03145', N'Mã giảm giá mới', N'Áp dụng cho mọi đơn hàng', Default, '2023-12-15', '2023-12-25', 25, 2, 20, 1, 2),
('VC03146', N'Khuyến mãi cuối năm', N'Áp dụng cho mọi đơn hàng', Default, '2023-12-20', '2023-12-30', 30, 1, 180000, 1, 1),
('VC03147', N'Tặng voucher mừng năm mới', N'Áp dụng cho mọi đơn hàng', Default, '2024-01-01', '2024-01-10', 10, 2, 25, 1, 2),
('VC03148', N'Ưu đãi đặc quyền', N'Áp dụng cho mọi đơn hàng', Default, '2024-01-05', '2024-01-15', 15, 1, 300000, 1, 1),
('VC03149', N'Mã giảm giá hấp dẫn', N'Áp dụng cho mọi đơn hàng', Default, '2024-01-10', '2024-01-20', 20, 2, 15, 1, 2),
('VC03150', N'Ưu đãi khách hàng thân thiết', N'Áp dụng cho mọi đơn hàng', Default, '2024-01-15', '2024-01-25', 25, 1, 200000, 1, 1),
('VC03151', N'Mã giảm giá mới nhất', N'Áp dụng cho mọi đơn hàng', Default, '2024-01-20', '2024-01-30', 30, 2, 30, 1, 2);


select * from SanPham
	 select * from MauSac
	 select * from ChatLieu
	 select * from HinhDang
	 select * from Anh
	 select * from SPCT
	 select * from HDCT
	 select * from HoaDon
	 select * from KhachHang
	 select * from Voucher
	 select * from Taikhoan