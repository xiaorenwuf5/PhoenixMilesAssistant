package com.codex.phoenixmiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FlightParserTest {
    @Test
    public void parsesTimedAirportLinesBeforeLooseText() {
        String text = "消费商\n"
                + "05-22周五 共2小时30分钟\n"
                + "13:00 0江北国际机场T3\n"
                + "0航班详情\n"
                + "听国航CA1430\n"
                + "波音737中型机\n"
                + "|有餐食|到达准点率100.0%\n"
                + "15:30 0首都国际机场T3\n"
                + "机票详情\n"
                + "成人 1640 经济舱(V1)\n"
                + "儿童 1440 全价经济舱(Y)\n"
                + "退改签说明\n"
                + "行李额说明";

        FlightInput input = FlightParser.parse(text);

        assertEquals("CA1430", input.flightNumber);
        assertEquals(5, input.travelDate.getMonthValue());
        assertEquals(22, input.travelDate.getDayOfMonth());
        assertEquals("CKG", input.originCode);
        assertEquals("PEK", input.destinationCode);
        assertEquals("V", input.bookingClass);
    }

    @Test
    public void parsesCityOnlyRouteHintNearFlightNumber() {
        String text = "航空累积\n"
                + "交易时间\n"
                + "2026/05/20\n"
                + "主题\n"
                + "2026.05.20 CA4132/CA4132北京 重\n"
                + "庆Q舱\n"
                + "可消费里程\n"
                + "+1453";

        FlightInput input = FlightParser.parse(text);

        assertEquals("CA4132", input.flightNumber);
        assertEquals("PEK", input.originCode);
        assertEquals("CKG", input.destinationCode);
        assertEquals("Q", input.bookingClass);
    }

    @Test
    public void resolvesCustomerFriendlyAirportInput() {
        assertEquals("CKG", AirportCatalog.codeFromUserInput("重庆"));
        assertEquals("CKG", AirportCatalog.codeFromUserInput("江北"));
        assertEquals("CKG", AirportCatalog.codeFromUserInput("重庆 江北 (CKG)"));
        assertEquals("PEK", AirportCatalog.codeFromUserInput("首都"));
        assertEquals("PEK", AirportCatalog.codeFromUserInput("北京"));
        assertEquals("CKG", AirportCatalog.codeFromUserInput("CKG"));
    }

    @Test
    public void keepsLongAirportMatchWhenShortNameIsAmbiguous() {
        assertEquals("SYX", AirportCatalog.findAllIn("三亚凤凰机场").get(0).code);
        assertEquals(1, AirportCatalog.findAllIn("三亚凤凰机场").size());
        assertTrue(AirportCatalog.findAllIn("CONLTENT").isEmpty());
    }

    @Test
    public void parsesTimedAirportLinesWhenOcrDropsAirportWord() {
        String text = "05-22周五 共2小时30分钟\n"
                + "13:00 江北T3\n"
                + "听国航CA1430\n"
                + "15:30 首都T3\n"
                + "成人 1640 经济舱(V1)";

        FlightInput input = FlightParser.parse(text);

        assertEquals("CKG", input.originCode);
        assertEquals("PEK", input.destinationCode);
    }
}
