package com.codex.phoenixmiles;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final String OFFICIAL_CALCULATOR_URL = "https://ffp.airchina.com.cn/plan/mileage_accumulate_calculator.html";

    private EditText flightField;
    private EditText dateField;
    private EditText originField;
    private EditText destinationField;
    private EditText bookingClassField;
    private EditText rawTextField;
    private TextView statusText;
    private TextView resultText;
    private ImageView imagePreview;
    private String lastResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            processImage(data.getData());
        }
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.setBackgroundColor(getColorCompat(com.codex.phoenixmiles.R.color.surface));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(28));
        scrollView.addView(root);

        TextView title = text("凤凰知音累计助手", 24, true);
        root.addView(title);

        TextView subtitle = text("从阿里商旅截图识别航班、日期、航段和舱位，然后自动查询国航官方累计计算器。", 14, false);
        subtitle.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_secondary));
        subtitle.setPadding(0, dp(6), 0, dp(14));
        root.addView(subtitle);

        LinearLayout actionRow = row();
        Button pickButton = primaryButton("选择截图");
        pickButton.setOnClickListener(v -> chooseImage());
        actionRow.addView(pickButton, weightParams());

        Button sampleButton = secondaryButton("载入示例");
        sampleButton.setOnClickListener(v -> loadSample());
        actionRow.addView(sampleButton, weightParamsWithMargin(dp(10)));
        root.addView(actionRow);

        imagePreview = new ImageView(this);
        imagePreview.setAdjustViewBounds(true);
        imagePreview.setMaxHeight(dp(260));
        imagePreview.setVisibility(View.GONE);
        imagePreview.setPadding(0, dp(12), 0, dp(6));
        root.addView(imagePreview);

        statusText = text("也可以在相册或阿里商旅截图后，点分享，选择这个 App。", 13, false);
        statusText.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_secondary));
        statusText.setPadding(0, dp(10), 0, dp(10));
        root.addView(statusText);

        LinearLayout form = panel();
        flightField = field("航班号，例如 CA4132");
        dateField = field("日期，例如 2026-05-20");
        originField = field("出发三字码，例如 PEK");
        destinationField = field("到达三字码，例如 CKG");
        bookingClassField = field("舱位代码，例如 S");
        bookingClassField.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(1)});

        form.addView(label("航班号"));
        form.addView(flightField);
        form.addView(label("乘机日期"));
        form.addView(dateField);
        form.addView(label("出发 / 到达"));
        LinearLayout airportRow = row();
        airportRow.addView(originField, weightParams());
        airportRow.addView(destinationField, weightParamsWithMargin(dp(10)));
        form.addView(airportRow);
        form.addView(label("舱位"));
        form.addView(bookingClassField);
        root.addView(form);

        LinearLayout calcRow = row();
        Button calcButton = primaryButton("计算累计");
        calcButton.setOnClickListener(v -> calculateFromFields());
        calcRow.addView(calcButton, weightParams());

        Button officialButton = secondaryButton("打开官方");
        officialButton.setOnClickListener(v -> openOfficialCalculator());
        calcRow.addView(officialButton, weightParamsWithMargin(dp(10)));
        root.addView(calcRow);

        resultText = text("等待识别或手工输入。", 15, false);
        resultText.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_primary));
        resultText.setBackgroundResource(com.codex.phoenixmiles.R.drawable.result_background);
        resultText.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        resultParams.setMargins(0, dp(14), 0, dp(10));
        root.addView(resultText, resultParams);

        Button copyButton = secondaryButton("复制结果");
        copyButton.setOnClickListener(v -> copyResult());
        root.addView(copyButton);

        TextView rawLabel = label("识别文本");
        rawLabel.setPadding(0, dp(18), 0, dp(6));
        root.addView(rawLabel);

        rawTextField = field("OCR 原文会显示在这里，也可以直接粘贴阿里商旅页面文字。");
        rawTextField.setMinLines(5);
        rawTextField.setGravity(Gravity.TOP | Gravity.START);
        root.addView(rawTextField, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(140)
        ));

        Button parseTextButton = secondaryButton("从文本重新识别");
        parseTextButton.setOnClickListener(v -> parseAndFill(rawTextField.getText().toString()));
        root.addView(parseTextButton);

        TextView footer = text("当前版本优先查询国航官方累计计算器；网络失败时才显示本地估算兜底。最终入账以国航账户实际入账为准。", 12, false);
        footer.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_secondary));
        footer.setPadding(0, dp(16), 0, 0);
        root.addView(footer);

        setContentView(scrollView);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                processImage(uri);
            }
            return;
        }

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (text != null) {
                rawTextField.setText(text);
                parseAndFill(text);
            }
            return;
        }

        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (text != null) {
                rawTextField.setText(text);
                parseAndFill(text.toString());
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "选择阿里商旅截图"), REQUEST_PICK_IMAGE);
    }

    private void processImage(Uri uri) {
        try {
            imagePreview.setImageURI(uri);
            imagePreview.setVisibility(View.VISIBLE);
            statusText.setText("正在识别截图文字...");
            InputImage image = InputImage.fromFilePath(this, uri);
            TextRecognizer recognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        String recognized = text.getText();
                        rawTextField.setText(recognized);
                        statusText.setText("识别完成，可以检查字段后计算。");
                        parseAndFill(recognized);
                    })
                    .addOnFailureListener(e -> {
                        statusText.setText("识别失败：" + e.getMessage());
                        Toast.makeText(this, "OCR 失败，可以手工输入字段。", Toast.LENGTH_LONG).show();
                    });
        } catch (IOException error) {
            statusText.setText("无法读取图片：" + error.getMessage());
            Toast.makeText(this, "无法读取图片", Toast.LENGTH_LONG).show();
        }
    }

    private void parseAndFill(String text) {
        FlightInput parsed = FlightParser.parse(text);
        fillFields(parsed);
        if (parsed.hasRequiredFields()) {
            queryOfficial(parsed);
        } else {
            StringBuilder missing = new StringBuilder("已尽量识别，仍需补充：");
            if (parsed.flightNumber.isEmpty()) {
                missing.append("航班号 ");
            }
            if (parsed.travelDate == null) {
                missing.append("日期 ");
            }
            if (parsed.originCode.isEmpty()) {
                missing.append("出发 ");
            }
            if (parsed.destinationCode.isEmpty()) {
                missing.append("到达 ");
            }
            if (parsed.bookingClass.isEmpty()) {
                missing.append("舱位 ");
            }
            statusText.setText(missing.toString().trim());
        }
    }

    private void fillFields(FlightInput input) {
        if (input.flightNumber != null && !input.flightNumber.isEmpty()) {
            flightField.setText(input.flightNumber);
        }
        if (input.travelDate != null) {
            dateField.setText(input.dateText());
        }
        if (input.originCode != null && !input.originCode.isEmpty()) {
            originField.setText(input.originCode);
        }
        if (input.destinationCode != null && !input.destinationCode.isEmpty()) {
            destinationField.setText(input.destinationCode);
        }
        if (input.bookingClass != null && !input.bookingClass.isEmpty()) {
            bookingClassField.setText(input.bookingClass);
        }
    }

    private void calculateFromFields() {
        FlightInput input = new FlightInput();
        input.flightNumber = value(flightField).toUpperCase(Locale.US).replace(" ", "");
        input.travelDate = parseDate(value(dateField));
        input.originCode = value(originField).toUpperCase(Locale.US);
        input.destinationCode = value(destinationField).toUpperCase(Locale.US);
        input.bookingClass = value(bookingClassField).toUpperCase(Locale.US);
        input.sourceText = rawTextField.getText().toString();
        FlightInput parsed = FlightParser.parse(input.sourceText);
        input.extraNonStatusMiles = parsed.extraNonStatusMiles;
        queryOfficial(input);
    }

    private void showResult(MileageResult result) {
        lastResult = ResultFormatter.format(result);
        resultText.setText(lastResult);
    }

    private void queryOfficial(FlightInput input) {
        statusText.setText("正在查询国航官方累计计算器...");
        resultText.setText("正在查询国航官方数据，请稍候。");
        new Thread(() -> {
            try {
                OfficialMileageResult officialResult = OfficialMileageClient.query(input, "Normal");
                String formatted = OfficialResultFormatter.format(input, officialResult);
                runOnUiThread(() -> {
                    statusText.setText("国航官方查询完成。");
                    lastResult = formatted;
                    resultText.setText(formatted);
                });
            } catch (Exception error) {
                MileageResult fallback = MileageCalculator.calculate(input);
                String formatted = "国航官方查询失败，以下为本地估算兜底：\n"
                        + error.getMessage()
                        + "\n\n"
                        + ResultFormatter.format(fallback);
                runOnUiThread(() -> {
                    statusText.setText("国航官方查询失败，已显示本地估算。");
                    lastResult = formatted;
                    resultText.setText(formatted);
                });
            }
        }).start();
    }

    private void openOfficialCalculator() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_CALCULATOR_URL));
        startActivity(intent);
    }

    private void copyResult() {
        if (lastResult == null || lastResult.trim().isEmpty()) {
            lastResult = resultText.getText().toString();
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("凤凰知音累计结果", lastResult));
        Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show();
    }

    private void loadSample() {
        String sample = "05-20 周三 共2小时50分钟\n"
                + "12:00 首都国际机场T3\n"
                + "国航 CA4132\n"
                + "14:50 江北国际机场T3\n"
                + "机票详情 成人 1590 舱等 经济舱(S)\n"
                + "预订权益说明 赠额外500里程";
        rawTextField.setText(sample);
        parseAndFill(sample);
    }

    private LocalDate parseDate(String value) {
        FlightInput parsed = FlightParser.parse(value == null ? "" : value);
        return parsed.travelDate;
    }

    private String value(EditText field) {
        return field.getText().toString().trim();
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_primary));
        textView.setLineSpacing(0, 1.12f);
        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private TextView label(String value) {
        TextView label = text(value, 13, true);
        label.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_secondary));
        label.setPadding(0, dp(10), 0, dp(6));
        return label;
    }

    private EditText field(String hint) {
        EditText editText = new EditText(this);
        editText.setTextSize(16);
        editText.setSingleLine(false);
        editText.setHint(hint);
        editText.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_primary));
        editText.setHintTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_secondary));
        editText.setBackgroundResource(com.codex.phoenixmiles.R.drawable.edit_text_background);
        editText.setPadding(dp(12), dp(8), dp(12), dp(8));
        return editText;
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(0xFFFFFFFF);
        button.setAllCaps(false);
        button.setMinHeight(dp(46));
        button.setBackgroundResource(com.codex.phoenixmiles.R.drawable.button_primary);
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_primary));
        button.setAllCaps(false);
        button.setMinHeight(dp(46));
        button.setBackgroundResource(com.codex.phoenixmiles.R.drawable.button_secondary);
        return button;
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(8), dp(14), dp(14));
        panel.setBackgroundResource(com.codex.phoenixmiles.R.drawable.panel_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(12), 0, dp(12));
        panel.setLayoutParams(params);
        return panel;
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private LinearLayout.LayoutParams weightParams() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    }

    private LinearLayout.LayoutParams weightParamsWithMargin(int leftMargin) {
        LinearLayout.LayoutParams params = weightParams();
        params.setMargins(leftMargin, 0, 0, 0);
        return params;
    }

    private int getColorCompat(int resId) {
        return getResources().getColor(resId, getTheme());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
