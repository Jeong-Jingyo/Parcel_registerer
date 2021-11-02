package com.laondruk.parcel

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo
import java.util.*

class ImageProcessing {
    companion object {
        private val queue: Queue<String> = LinkedList<String>()
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private lateinit var callback: (Any?) -> Unit

    fun setProcessingCallback(_callback: (Any?) -> Unit) {
        callback = _callback
    }

    fun offer(imagePath: String) {
        queue.add(imagePath)
    }

    fun startProcessingQueue() {
        while (true) {
            if (queue.size == 0) {
                break
            }
            detect(Imgcodecs.imread(queue.poll()))
        }
    }


    fun detect(image: Mat) = recognizeText(preprocessImage(image))

    // 이미지 이진화
    fun preprocessImage(image: Mat): Mat {
        Imgproc.resize(image, image, Size(1440.0, 1080.0))

        // 회색조 변환
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)

        // 노이즈 감쇄
        Photo.fastNlMeansDenoising(image, image, 100.toFloat(), 9, 9)

        // 이진화
        Imgproc.adaptiveThreshold(
            image, image,
            255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 17, 5.0
        )
        return image
    }


    // 이미지에서 학년반번호 감지
    fun recognizeText(image: Mat): String {
        val bitmap = createBitmap(image.width(), image.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(image, bitmap)

        MainActivity.tessBaseAPI.setImage(bitmap)

        MainActivity.tessBaseAPI.setVariable(
            TessBaseAPI.VAR_CHAR_BLACKLIST,
            "|/\\'°_{}`〈〉「」、『』【】^|〓×\$~《±。′“”%\"\'《》,.†〔〕ː〃DEFGHIJKLMNOPQRSTUVWXYZdefghijklmnopqrstuvwxyz"
        )
        MainActivity.tessBaseAPI.setVariable(
            TessBaseAPI.VAR_CHAR_WHITELIST,
            """|0123456789
                |가각간갈감갑강개객갱갹거건걸검겁게격견결겸경계고곡곤골공곶과곽관괄광괘괴굉교구국군굴궁
                |권궐궤귀규균귤극근글금급긍기긴길김끽나낙난날남납낭내녀년념녕노농뇨눈눌뇌뉴능니닉다단달담답당대댁덕
                |도독돈돌동두둔득등라락란랄람랍랑래랭략량려력련렬렴렵령례로록론롱뢰료룡루류륙륜률륭륵름릉리린림립마
                |막만말망매맥맹멱면멸명몌모목몰몽묘무묵문물미민밀박반발방배백번벌범법벽변별병보복본볼봉부북분불붕비
                |빈빙사삭산살삼삽상쌍새색생서석선설섬섭성세소속손솔송쇄쇠수숙순술숭슬습승시씨식신실심십아악안알암압
                |앙애액앵야약양어억언얼엄업엔여역연열염엽영예오옥온올옹와완왈왕왜외요욕용우욱운울웅원월위유육윤율융
                |은을음읍응의이익인일임입잉자작잔잠잡장재쟁저적전절점접정제조족존졸종좌죄주죽준줄중즉즐즙증지직진질
                |짐집징차착찬찰참창채책처척천철첨첩청체초촉촌총촬최추축춘출충췌취측층치칙친칠침칩칭쾌타탁탄탈탐탑탕
                |태택탱터토통퇴투특틈파판팔패팽퍅편폄평폐포폭표품풍피필핍하학한할함합항해핵행향허헌헐험혁현혈혐협형
                |혜호혹혼홀홍화확환활황회획횡효후훈훙훤훼휘휴휼흉흑흔흘흠흡흥희힐
                |*ABab학년반번호-""".trimMargin()  // 숫자, 인명용한자 등...
        )

        return MainActivity.tessBaseAPI.utF8Text
    }
}

class ImageOnLine(
    private val context: Context,
    private val frameView: FrameLayout,
    private val onImageProcessedCallback: ((Boolean) -> Unit)? = null
) {
    private val imageView = ImageView(context)

    fun setProcessingStatIcon(processingCompleted: Int? = null) {
        frameView.removeView(imageView)
        if (processingCompleted == null) {
            imageView.setImageResource(R.drawable.ic_round_pending_24)
        } else if (processingCompleted == 0) { // 검색 정상(결과 하나만)
            imageView.setImageResource(R.drawable.ic_round_check_circle_24)
        } else if (processingCompleted == 1) { // 검색 결과 여러개
            imageView.setImageResource(R.drawable.ic_round_warning_24)
        } else if (processingCompleted == -1) { // 데이터 찾을 수 없음
            imageView.setImageResource(R.drawable.ic_round_cancel_24)
        }

        val iconSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            48f,
            Resources.getSystem().displayMetrics
        ).toInt()

        frameView.addView(imageView, iconSize, iconSize)
    }
}