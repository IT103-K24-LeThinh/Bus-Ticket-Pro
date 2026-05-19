-- =====================================================================
-- Migration script: Rename PaymentMethod ONLINE -> BANK_TRANSFER
-- =====================================================================
--
-- Bối cảnh:
--   Enum `PaymentMethod` được đổi tên giá trị `ONLINE` thành `BANK_TRANSFER`
--   để phản ánh đúng phương thức thanh toán chuyển khoản ngân hàng (theo
--   Requirement 3.6 của spec `passenger-features`).
--
--   Cột `payment_method` trong bảng `bookings` được lưu dưới dạng STRING
--   (`@Enumerated(EnumType.STRING)`), do đó các bản ghi hiện tại đang
--   chứa giá trị `'ONLINE'` sẽ không khớp với enum mới và gây lỗi khi
--   ứng dụng đọc dữ liệu.
--
-- ⚠ LƯU Ý VẬN HÀNH:
--   - Project HIỆN TẠI KHÔNG dùng Flyway/Liquibase. File này chỉ là
--     script SQL hỗ trợ vận hành.
--   - PHẢI chạy script này TRƯỚC KHI deploy phiên bản mới (đã rename
--     enum) lên môi trường có DB chứa dữ liệu cũ. Nếu bỏ qua bước này,
--     ứng dụng sẽ ném `IllegalArgumentException` khi map giá trị
--     `'ONLINE'` về enum `PaymentMethod`.
--   - Trên môi trường DB rỗng (dev mới khởi tạo) hoặc DB chưa từng có
--     bản ghi `payment_method = 'ONLINE'`, có thể bỏ qua script.
--   - Nên backup bảng `bookings` trước khi chạy.
--
-- Cách chạy (ví dụ với MySQL):
--   mysql -u <user> -p <database> < V1__rename_payment_method_online_to_bank_transfer.sql
-- =====================================================================

UPDATE bookings
SET payment_method = 'BANK_TRANSFER'
WHERE payment_method = 'ONLINE';
