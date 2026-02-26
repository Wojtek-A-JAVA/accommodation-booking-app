package accommodation.booking.app.mapper;

import accommodation.booking.app.config.MapperConfig;
import accommodation.booking.app.dto.payment.PaymentDto;
import accommodation.booking.app.dto.payment.PaymentResponseDto;
import accommodation.booking.app.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    @Mapping(target = "bookingId", source = "bookingId.id")
    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    @Mapping(target = "amountToPay", source = "amountToPay")
    @Mapping(target = "sessionUrl", expression = "java(payment.getSessionUrl().toString())")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "message", ignore = true)
    @Mapping(target = "paymentId", source = "id")
    PaymentResponseDto toResponseDto(Payment payment);
}
