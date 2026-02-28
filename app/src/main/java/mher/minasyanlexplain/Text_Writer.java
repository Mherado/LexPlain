package mher.minasyanlexplain;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class Text_Writer {
    private String fullstring;
    private final Context context;

    public Text_Writer(Context context, String uriString) {
        this.context = context;
        PDFBoxResourceLoader.init(context);
        Uri fileUri = Uri.parse(uriString);
        processContract(fileUri);
    }

    public void processContract(Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) throw new IOException("Failed to open input stream");

            PDDocument document = PDDocument.load(is);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            is.close();

            // If text extraction fails or returns too little data, fall back to OCR
            if (text == null || text.trim().length() < 10) {
                this.fullstring = performSmartOCR(uri);
            } else {
                this.fullstring = text;
            }
        } catch (Exception e) {
            Log.e("Text_Writer", "PDF extraction failed, attempting OCR: " + e.getMessage());
            this.fullstring = performSmartOCR(uri);
        }
        Log.d("Text_Writer", "Processing result: " + fullstring);
    }

    private String performSmartOCR(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(context, uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            Text result = Tasks.await(recognizer.process(image));
            return result.getText();

        } catch (IOException | ExecutionException | InterruptedException e) {
            Log.e("Text_Writer", "OCR Error: " + e.getMessage());
            return "Error during text processing";
        }
    }

    public String getFullstring() {
        return fullstring;
    }
}