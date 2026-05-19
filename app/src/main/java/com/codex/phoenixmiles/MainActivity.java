package com.codex.phoenixmiles;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final String OFFICIAL_CALCULATOR_URL = "https://ffp.airchina.com.cn/plan/mileage_accumulate_calculator.html";
    private static final int DEFAULT_MEMBER_TIER_INDEX = 3;
    private static final String[] MEMBER_GRADE_VALUES = {
            "Normal", "Junior", "Silver", "Gold", "Platinum", "LifetimePlatinum"
    };

    private EditText flightField;
    private EditText dateField;
    private EditText originField;
    private EditText destinationField;
    private EditText bookingClassField;
    private EditText rawTextField;
    private Spinner memberTierSpinner;
    private TextView statusText;
    private TextView resultText;
    private TextView imageHintText;
    private ImageView imagePreview;
    private LinearLayout imageNavRow;
    private Button previousImageButton;
    private Button nextImageButton;
    private Uri currentImageUri;
    private List<Uri> currentImageUris = new ArrayList<>();
    private int currentImageIndex;
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
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            processImages(extractImageUris(data));
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
        Button pickButton = primaryButton("选择截图（可多选）");
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
        imagePreview.setContentDescription("点击截图可放大核对");
        imagePreview.setOnClickListener(v -> openCurrentImage());
        root.addView(imagePreview);

        imageNavRow = row();
        previousImageButton = secondaryButton("上一张");
        previousImageButton.setOnClickListener(v -> showAdjacentImage(-1));
        imageNavRow.addView(previousImageButton, weightParams());

        nextImageButton = secondaryButton("下一张");
        nextImageButton.setOnClickListener(v -> showAdjacentImage(1));
        imageNavRow.addView(nextImageButton, weightParamsWithMargin(dp(10)));
        imageNavRow.setVisibility(View.GONE);
        root.addView(imageNavRow);

        imageHintText = text("点击截图可放大核对", 12, false);
        imageHintText.setTextColor(getColorCompat(com.codex.phoenixmiles.R.color.text_secondary));
        imageHintText.setVisibility(View.GONE);
        root.addView(imageHintText);

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
        memberTierSpinner = createMemberTierSpinner();

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
        form.addView(label("会员级别"));
        form.addView(memberTierSpinner);
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
        if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null && type.startsWith("image/")) {
            processImages(extractImageUris(intent));
            return;
        }

        if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                List<Uri> uris = new ArrayList<>();
                uris.add(uri);
                processImages(uris);
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "选择一张或多张阿里商旅截图"), REQUEST_PICK_IMAGE);
    }

    private List<Uri> extractImageUris(Intent data) {
        List<Uri> uris = new ArrayList<>();
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                if (uri != null) {
                    uris.add(uri);
                }
            }
        }

        Uri uri = data.getData();
        if (uri != null && !containsUri(uris, uri)) {
            uris.add(uri);
        }

        ArrayList<Uri> sharedUris = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (sharedUris != null) {
            for (Uri sharedUri : sharedUris) {
                if (sharedUri != null && !containsUri(uris, sharedUri)) {
                    uris.add(sharedUri);
                }
            }
        }
        return uris;
    }

    private boolean containsUri(List<Uri> uris, Uri uri) {
        for (Uri item : uris) {
            if (item.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    private void processImages(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            Toast.makeText(this, "没有选择可识别的图片", Toast.LENGTH_LONG).show();
            return;
        }

        currentImageUris = new ArrayList<>(uris);
        currentImageIndex = 0;
        updateImagePreview();
        imageHintText.setVisibility(View.VISIBLE);
        clearParsedFields();
        rawTextField.setText("");

        TextRecognizer recognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
        recognizeNextImage(uris, 0, new StringBuilder(), recognizer, 0);
    }

    private void recognizeNextImage(List<Uri> uris, int index, StringBuilder combinedText, TextRecognizer recognizer, int successCount) {
        if (index >= uris.size()) {
            recognizer.close();
            String recognized = combinedText.toString().trim();
            rawTextField.setText(recognized);
            if (successCount == 0 || recognized.isEmpty()) {
                statusText.setText("截图识别失败，可以手工输入字段。");
                Toast.makeText(this, "OCR 失败，可以手工输入字段。", Toast.LENGTH_LONG).show();
                return;
            }

            statusText.setText("已合并识别 " + successCount + " 张截图，可切换预览并放大核对；请检查字段后计算。");
            parseAndFill(recognized);
            return;
        }

        statusText.setText("正在识别截图 " + (index + 1) + "/" + uris.size() + "...");
        try {
            InputImage image = InputImage.fromFilePath(this, uris.get(index));
            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        appendRecognizedText(combinedText, index, formatRecognizedText(text));
                        recognizeNextImage(uris, index + 1, combinedText, recognizer, successCount + 1);
                    })
                    .addOnFailureListener(e -> {
                        appendRecognizedText(combinedText, index, "识别失败：" + e.getMessage());
                        recognizeNextImage(uris, index + 1, combinedText, recognizer, successCount);
                    });
        } catch (IOException error) {
            appendRecognizedText(combinedText, index, "无法读取图片：" + error.getMessage());
            recognizeNextImage(uris, index + 1, combinedText, recognizer, successCount);
        }
    }

    private void appendRecognizedText(StringBuilder builder, int imageIndex, String text) {
        if (builder.length() > 0) {
            builder.append("\n\n");
        }
        builder.append("【截图")
                .append(imageIndex + 1)
                .append("】\n")
                .append(text == null ? "" : text.trim());
    }

    private String formatRecognizedText(Text recognizedText) {
        if (recognizedText == null) {
            return "";
        }

        List<OcrLine> lines = new ArrayList<>();
        for (Text.TextBlock block : recognizedText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String text = line.getText();
                if (text == null || text.trim().isEmpty()) {
                    continue;
                }
                lines.add(new OcrLine(text.trim(), line.getBoundingBox()));
            }
        }

        if (lines.isEmpty()) {
            return recognizedText.getText();
        }

        Collections.sort(lines, (left, right) -> {
            int tolerance = Math.max(16, Math.min(left.height, right.height));
            int centerDiff = Math.abs(left.centerY - right.centerY);
            if (centerDiff <= tolerance && left.left != right.left) {
                return Integer.compare(left.left, right.left);
            }
            if (left.centerY != right.centerY) {
                return Integer.compare(left.centerY, right.centerY);
            }
            return Integer.compare(left.left, right.left);
        });

        StringBuilder builder = new StringBuilder();
        for (OcrLine line : lines) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(line.text);
        }
        return builder.toString();
    }

    private String previewHint(int imageCount) {
        if (imageCount <= 1) {
            return "点击截图可放大核对";
        }
        return "已选择 " + imageCount + " 张截图，当前第 " + (currentImageIndex + 1) + " 张，点击可放大核对";
    }

    private void updateImagePreview() {
        if (currentImageUris == null || currentImageUris.isEmpty()) {
            currentImageUri = null;
            imagePreview.setVisibility(View.GONE);
            imageNavRow.setVisibility(View.GONE);
            imageHintText.setVisibility(View.GONE);
            return;
        }

        if (currentImageIndex < 0) {
            currentImageIndex = 0;
        }
        if (currentImageIndex >= currentImageUris.size()) {
            currentImageIndex = currentImageUris.size() - 1;
        }

        currentImageUri = currentImageUris.get(currentImageIndex);
        imagePreview.setImageURI(currentImageUri);
        imagePreview.setVisibility(View.VISIBLE);
        String hint = previewHint(currentImageUris.size());
        imagePreview.setContentDescription(hint);
        imageHintText.setText(hint);
        imageNavRow.setVisibility(currentImageUris.size() > 1 ? View.VISIBLE : View.GONE);
    }

    private void showAdjacentImage(int direction) {
        if (currentImageUris == null || currentImageUris.isEmpty()) {
            return;
        }
        currentImageIndex += direction;
        if (currentImageIndex < 0) {
            currentImageIndex = currentImageUris.size() - 1;
        } else if (currentImageIndex >= currentImageUris.size()) {
            currentImageIndex = 0;
        }
        updateImagePreview();
    }

    private void openCurrentImage() {
        if (currentImageUri == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(currentImageUri, "image/*");
        intent.setClipData(ClipData.newUri(getContentResolver(), "截图", currentImageUri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (RuntimeException error) {
            Toast.makeText(this, "无法打开原图，请在相册中查看。", Toast.LENGTH_LONG).show();
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

    private void clearParsedFields() {
        flightField.setText("");
        dateField.setText("");
        originField.setText("");
        destinationField.setText("");
        bookingClassField.setText("");
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
        String memberGrade = selectedMemberGradeValue();
        String memberTierLabel = selectedMemberTierLabel();
        statusText.setText("正在按" + memberTierLabel + "查询国航官方累计计算器...");
        resultText.setText("正在查询国航官方数据，请稍候。");
        new Thread(() -> {
            try {
                OfficialMileageResult officialResult = OfficialMileageClient.query(input, memberGrade);
                FlightInput resultInput = input;
                boolean swappedRoute = false;
                if (!officialResult.success && officialResult.isRouteMismatch()) {
                    FlightInput swappedInput = swappedRouteInput(input);
                    if (swappedInput != null) {
                        OfficialMileageResult swappedResult = OfficialMileageClient.query(swappedInput, memberGrade);
                        if (swappedResult.success) {
                            officialResult = swappedResult;
                            resultInput = swappedInput;
                            swappedRoute = true;
                        }
                    }
                }

                FlightInput finalInput = resultInput;
                boolean finalSwappedRoute = swappedRoute;
                String formatted = OfficialResultFormatter.format(finalInput, officialResult, memberTierLabel);
                String status = finalSwappedRoute
                        ? "已按国航官方结果自动纠正出发/到达机场。"
                        : officialStatusText(officialResult);
                runOnUiThread(() -> {
                    if (finalSwappedRoute) {
                        fillFields(finalInput);
                    }
                    statusText.setText(status);
                    lastResult = formatted;
                    resultText.setText(formatted);
                });
            } catch (Exception error) {
                MileageResult fallback = MileageCalculator.calculate(input);
                String formatted = "国航官方查询失败，以下为本地估算兜底：\n"
                        + error.getMessage()
                        + "\n\n"
                        + "本地估算暂未计算" + memberTierLabel + "额外奖励，最终请以国航官方结果和实际入账为准。\n\n"
                        + ResultFormatter.format(fallback);
                runOnUiThread(() -> {
                    statusText.setText("国航官方查询失败，已显示本地估算。");
                    lastResult = formatted;
                    resultText.setText(formatted);
                });
            }
        }).start();
    }

    private FlightInput swappedRouteInput(FlightInput input) {
        if (input == null
                || input.originCode == null
                || input.destinationCode == null
                || input.originCode.isEmpty()
                || input.destinationCode.isEmpty()
                || input.originCode.equals(input.destinationCode)) {
            return null;
        }

        FlightInput swapped = new FlightInput();
        swapped.flightNumber = input.flightNumber;
        swapped.travelDate = input.travelDate;
        swapped.originCode = input.destinationCode;
        swapped.destinationCode = input.originCode;
        swapped.bookingClass = input.bookingClass;
        swapped.extraNonStatusMiles = input.extraNonStatusMiles;
        swapped.sourceText = input.sourceText;
        return swapped;
    }

    private String selectedMemberGradeValue() {
        int position = memberTierSpinner == null ? DEFAULT_MEMBER_TIER_INDEX : memberTierSpinner.getSelectedItemPosition();
        if (position < 0 || position >= MEMBER_GRADE_VALUES.length) {
            return MEMBER_GRADE_VALUES[DEFAULT_MEMBER_TIER_INDEX];
        }
        return MEMBER_GRADE_VALUES[position];
    }

    private String selectedMemberTierLabel() {
        if (memberTierSpinner == null || memberTierSpinner.getSelectedItem() == null) {
            return "金卡";
        }
        return memberTierSpinner.getSelectedItem().toString();
    }

    private String officialStatusText(OfficialMileageResult result) {
        if (result == null) {
            return "国航官方没有返回可用结果。";
        }
        if (result.success) {
            return "国航官方查询完成。";
        }
        if (result.isRouteMismatch()) {
            return "航班号和出发/到达机场不匹配，请核对截图。";
        }
        return "国航官方返回提示，请核对字段。";
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

    private Spinner createMemberTierSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                com.codex.phoenixmiles.R.array.member_tiers,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(DEFAULT_MEMBER_TIER_INDEX);
        spinner.setMinimumHeight(dp(46));
        spinner.setPadding(dp(8), dp(6), dp(8), dp(6));
        return spinner;
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

    private static final class OcrLine {
        final String text;
        final int left;
        final int centerY;
        final int height;

        OcrLine(String text, Rect box) {
            this.text = text;
            if (box == null) {
                left = 0;
                centerY = 0;
                height = 16;
            } else {
                left = box.left;
                centerY = box.centerY();
                height = Math.max(1, box.height());
            }
        }
    }
}
