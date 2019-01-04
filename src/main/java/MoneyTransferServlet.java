import com.fasterxml.jackson.databind.ObjectMapper;
import json.MoneyTransferRequest;
import service.MoneyTransferService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@WebServlet(name = "MoneyTransferServlet", urlPatterns = {"/"}, loadOnStartup = 1)
public class MoneyTransferServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final MoneyTransferService moneyTransferService = MoneyTransferService.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            MoneyTransferRequest moneyTransferRequest =
                    OBJECT_MAPPER.readValue(req.getReader(), MoneyTransferRequest.class);
            validate(moneyTransferRequest);
            moneyTransferService.transfer(moneyTransferRequest);
        } catch(Exception e) {
            resp.sendError(SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        resp.setStatus(SC_OK);
    }

    private void validate(MoneyTransferRequest moneyTransferRequest) {
        if (moneyTransferRequest.getAmount() < 0) {
            throw new IllegalArgumentException(
                    format("Amount to transfer should be above zero, %s", moneyTransferRequest.getAmount()));
        }
    }
}