import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.google.api.services.sheets.v4.Sheets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;


public class TelegramBot extends TelegramLongPollingBot {
    private String studentNumber;

    final String botName;
    final String botToken;

    public TelegramBot(String botName, String botToken) throws GeneralSecurityException, IOException {
        this.botName = botName;
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }


    @Override
    public void onUpdateReceived(Update update) {

        String messageText = update.getMessage().getText();
        String chatId = String.valueOf(update.getMessage().getChatId());
        final String spreadsheetId = "1cSHVohLSiR-lVOFPlJMK9sZLRV3cFyds2f9XNjunE4I";
        final String spreadsheetId2 = "1m7v6SaP1FXJcC3MQfibwrUByc0E5zvFq1sHonyYhaJQ";
        if (messageText.startsWith("/login ")) {
            studentNumber = messageText.replace("/login ", "");
            sendTextMessage(chatId, "Отлично ваш номер: " + studentNumber);
        } else if (messageText.startsWith("/start")) {
            sendStartMessage(chatId);
        } else if (messageText.startsWith("/help")) {
            sendHelpMessage(chatId);
        } else if (messageText.startsWith("/StudentsList")) {
            try {
                sendSheetStudents(chatId, spreadsheetId);
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        } else if (messageText.startsWith("/MarksP")) {
            try {
                sendStudentMarksProgs(spreadsheetId, chatId, studentNumber);
            } catch (GeneralSecurityException | IOException | TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (messageText.startsWith("/Schedule")) {
            sendSchedule(chatId);
        } else if (messageText.startsWith("/MarksMDK03")) {
            try {
                sendMarksMDK03(spreadsheetId2,chatId,studentNumber);
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (messageText.contains("кот") || messageText.contains("кошка") || messageText.contains("cat") || messageText.contains("Кот") || messageText.contains("Кошка")) {
            try {
                sendMeow(chatId);
            } catch (IOException | TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendStartMessage(String chatId) {
        String responseText = "Привет!Первым делом стоит залогиниться.Напиши свой номер из списка через команду /login(номер по списку)\nЧтобы просмотреть все команды, вызови /help";
        sendTextMessage(chatId, responseText);

    }

    public void sendHelpMessage(String chatId) {
        String responseText = "Команды для выполнения:\n/login - выбор студента\n/StudentsList - список студентов\n/MarksP - баллы по ОАИПу\n/MarksMDK03 - зачеты по МДК 03\n/Schedule - расписание\n кот - кот\uD83D\uDE3A";
        sendTextMessage(chatId, responseText);
    }

    public void sendSheetStudents(String chatId, String spreadsheetId) throws GeneralSecurityException, IOException {
        Sheets service = SheetsQuickstart.getSheetsService();
        String range1 = "Сводник!A4:B33";
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range1).execute();
        List<List<Object>> values = response.getValues();

        StringBuilder responseText = new StringBuilder();

        if (values != null) {
            for (List<Object> row : values) {
                for (Object cell : row) {
                    responseText.append(cell).append(" ");
                }
                responseText.append("\n");
            }
        } else {
            responseText.append("Нет данных.");
        }

        sendTextMessage(chatId, responseText.toString());
    }


    public void sendStudentMarksProgs(String spreadsheetId, String chatId, String studentNumber) throws GeneralSecurityException, IOException, TelegramApiException {
        Sheets service = SheetsQuickstart.getSheetsService();
        int studentRow = 3 + Integer.parseInt(studentNumber);
        String rangeRow1 = "Сводник!B" + studentRow;
        String rangeRow2 = "Работа в аудитории!" + CellPerMonthP() + studentRow;

        ValueRange responseRow1 = service.spreadsheets().values().get(spreadsheetId, rangeRow1).execute();
        ValueRange responseRow2 = service.spreadsheets().values().get(spreadsheetId, rangeRow2).execute();

        List<List<Object>> valuesRow1 = responseRow1.getValues();
        List<List<Object>> valuesRow2 = responseRow2.getValues();

        if (valuesRow1 == null || valuesRow1.isEmpty() || valuesRow2 == null || valuesRow2.isEmpty()) {
            sendTextMessage(chatId, "Нет данных о студенте с таким номером.");
        } else {
            String studentInfo = "Информация о студенте: " + valuesRow1.get(0).get(0);
            String marksInfo = "Баллы за апрель: " + valuesRow2.get(0).get(0);

            sendTextMessage(chatId, studentInfo + "\n" + marksInfo);
        }
    }
    public void sendMarksMDK03(String spreadsheetId2, String chatId, String studentNumber) throws GeneralSecurityException, IOException {
        Sheets service = SheetsQuickstart.getSheetsService();
        int studentRow = 34 + Integer.parseInt(studentNumber);
        String rangeRow1 = "Лист1!C" + studentRow + ":AJ" + studentRow;

        ValueRange response = service.spreadsheets().values().get(spreadsheetId2, rangeRow1).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            sendTextMessage(chatId,"не сдана");
        } else {
            StringBuilder message = new StringBuilder();

            for (List<Object> row : values) {
                for (Object cell : row) {
                    message.append(cell).append(" ");
                }
            }

            sendTextMessage(chatId, message.toString());
        }
    }


    public void sendSchedule(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        DayOfWeek currentDayOfWeek = LocalDate.now().getDayOfWeek();
        switch (currentDayOfWeek) {
            case MONDAY:
                message.setText("\uD83C\uDF80ПОНЕДЕЛЬНИК\uD83C\uDF80\n" +
                        "13.40\n" +
                        "4\uFE0F⃣МДК.01.01 Основы проектирования цифровой техники(1307)\n" +
                        "5\uFE0F⃣ОАиП(305)\n" +
                        "6\uFE0F⃣Выш мат(310)\n" +
                        "18.30");
                break;
            case TUESDAY:
                message.setText("\uD83C\uDF80ВТОРНИК\uD83C\uDF80\n" +
                        "13.40\n" +
                        "4\uFE0F⃣МДК.03.01 Техническое обслуживание и ремонт компьютерных систем и комплексов\n" +
                        "5\uFE0F⃣МДК.03.01 Техническое обслуживание и ремонт компьютерных систем и комплексов\n" +
                        "6\uFE0F⃣МДК.03.01 Техническое обслуживание и ремонт компьютерных систем и комплексов\n" +
                        "(1307)\n" +
                        "18.30");
                break;
            case WEDNESDAY:
                message.setText("\uD83C\uDF80СРЕДА\uD83C\uDF80\n" +
                        "9.40\n" +
                        "2\uFE0F⃣Код будущего \n" +
                        "3\uFE0F⃣МДК.01.01 Основы проектирования цифровой техники(1307)\n" +
                        "4\uFE0F⃣Электротехника(401)\n" +
                        "5\uFE0F⃣МДК 01.01(1307)\n" +
                        "16.50");
                break;
            case THURSDAY:
                message.setText("\uD83C\uDF80ЧЕТВЕРГ\uD83C\uDF80\n" +
                        "11.50\n" +
                        "3\uFE0F⃣Основы финансовой грамотности(416)\n" +
                        "4\uFE0F⃣Выш мат(301)\n" +
                        "5\uFE0F⃣Дискретная математика(308)\n" +
                        "16.50");
                break;
            case FRIDAY:
                message.setText("\uD83C\uDF80ПЯТНИЦА\uD83C\uDF80\n" +
                        "13.40\n" +
                        "4\uFE0F⃣ОАиП(1202/1318)\n" +
                        "5\uFE0F⃣Электротехника(413)");
                break;
            case SATURDAY:
                message.setText("\uD83C\uDF80СУББОТА\uD83C\uDF80\n" +
                        "11.30\n" +
                        "3\uFE0F⃣Метрология и электро(410)\n" +
                        "4\uFE0F⃣Дискретная мат(308)\n" +
                        "5\uFE0F⃣Физра\n" +
                        "6\uFE0F⃣Иностранный язык(406/313)\n" +
                        "18.00");
            default:
                message.setText("В воскресенье нет пар.");
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void sendTextMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    String CellPerMonthP() {

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Месяцы в Calendar начинаются с 0

        String[] monthNames = new String[]{
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        String currentMonthName = monthNames[currentMonth - 1];

        switch (currentMonthName) {
            case "Апрель":
                return "CF";
            case "Май":
                return "CN";
            case "Июнь":
                return "CU";

            default:
                return "ошибка";
        }

    }
    String CellPerMonthM() {

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Месяцы в Calendar начинаются с 0

        String[] monthNames = new String[]{
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        String currentMonthName = monthNames[currentMonth - 1];

        switch (currentMonthName) {
            case "Апрель":
                return "AF:AL";
            case "Май":
                return "AM:AP";
            case "Июнь":
                return "AU:AX";

            default:
                return "ошибка";
        }

    }
    public void sendMeow(String chatId) throws IOException, TelegramApiException {
        URL url = new URL("https://api.thecatapi.com/v1/images/search");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                String jsonResponse = response.toString();
                JSONArray jsonArray = new JSONArray(jsonResponse);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String imageUrl = jsonObject.getString("url");

                sendPhotoFromUrl(chatId, imageUrl);
            }
        }

        connection.disconnect();
    }

    public void sendPhotoFromUrl(String chatId, String imageUrl) throws TelegramApiException {
        SendPhoto message = new SendPhoto();
        message.setChatId(chatId);
        message.setPhoto(new InputFile(imageUrl));
        execute(message);
    }
}








