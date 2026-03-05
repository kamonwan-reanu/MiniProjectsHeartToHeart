using UnityEngine;
using UnityEngine.UI; // สำคัญมาก: ถ้าไม่มีอันนี้จะสั่ง Text ไม่ได้
using UnityEngine.SceneManagement;

public class CreditScreen : MonoBehaviour { // ชื่อ Class ต้องตรงกับชื่อไฟล์ CreditScreen.cs

    public Text creditsText; // ต้องลาก Object Text ในหน้า UI มาใส่ที่ช่องนี้ใน Inspector
    public RectTransform creditsRect; // ใช้ควบคุมตำแหน่งการเลื่อน
    public float scrollSpeed = 30f;

    void Start() {
        // ใส่ข้อความที่ต้องการ (ใช้ @ เพื่อพิมพ์หลายบรรทัดได้)
        creditsText.text = @"
Project Manager: Pariyakon Thanponsri
Scenario Writer: Napatprapa Kulsuttisatien
Lead Developer: Niranrak Anuson
Asset & Resource Manager: Kamonwan Reanu
Tester: Krittaphat Mongkolklee
UI Designer: Narin Sawaijamniankul

Behind the Story
'จากความประทับใจในบทบาท... สู่การรังสรรค์ตัวละครมาเฟียในฝัน...'";
    }

    void Update() {
        // เลื่อนตำแหน่งขึ้นด้านบน (Y)
        creditsRect.anchoredPosition += new Vector2(0, scrollSpeed * Time.deltaTime);

        // ตรวจสอบการกดปุ่มเพื่อกลับหน้าเมนู
        if (Input.anyKeyDown) {
            SceneManager.LoadScene("MainMenu"); // ตรวจสอบว่าใน Build Settings มีชื่อฉากนี้อยู่
        }
    }
}