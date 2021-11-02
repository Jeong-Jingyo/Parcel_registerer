import cv2
import numpy as np
import time
import copy

path = "original3.png"

image = cv2.resize(cv2.imread(path), (1200, 900))
cv2.imshow("original.jpg", cv2.resize(image, (1200, 900)))
startTime = time.time()

# 이미지 샤프닝
# sharpening_filter = np.array([[-1, -1, -1, -1, -1],
#                               [-1, 2, 2, 2, -1],
#                               [-1, 2, 9, 2, -1],
#                               [-1, 2, 2, 2, -1],
#                               [-1, -1, -1, -1, -1]]) / 9.0
#
# timage = cv2.filter2D(timage, -1, sharpening_filter)

# 회색조 변환
image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
print(f"{time.time() - startTime}: cvtColor")

# 노이즈 감쇄
image = cv2.fastNlMeansDenoising(image, 200, 7, 5)
timage = cv2.GaussianBlur(image, (7, 7), 1)
timage = cv2.erode(timage, cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3)))

# 이진화
timage = cv2.adaptiveThreshold(timage, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 13, 5)
print(f"{time.time() - startTime}: adaptiveThreshold")

# todo: 원근 변환
timage = cv2.Canny(timage, 170, 20)
image = copy.deepcopy(timage)
timage = cv2.morphologyEx(timage, cv2.MORPH_CLOSE, np.ones((40, 40), np.uint8),
                          cv2.getStructuringElement(cv2.MORPH_CROSS, (40, 40)))
timage = cv2.erode(timage, np.ones((3, 3), np.uint8))

contours, hierarchy = cv2.findContours(timage, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_TC89_KCOS)

timage = cv2.cvtColor(timage, cv2.COLOR_GRAY2BGR)
for i in range(len(contours)):
    cv2.drawContours(timage, [contours[i]], 0, (0, 0, 255), 1)
    poly = cv2.approxPolyDP(contours[i], 3, True)

    if True:
        # timage = cv2.drawContours(timage, [poly], 0, (255, 255, 255), -1)
        timage = cv2.morphologyEx(timage, cv2.MORPH_OPEN, np.ones((10, 10), np.uint8))
        pass



cv2.imshow("destination.jpg", cv2.resize(image, (1200, 900)))
cv2.imshow("test.jpg", cv2.resize(timage, (1200, 900)))
cv2.waitKey()
