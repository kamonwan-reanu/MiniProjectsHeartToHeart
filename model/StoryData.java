package model;

/**
 * StoryData — ไฟล์เก็บเนื้อเรื่องทั้งหมด
 * 
 * โครงสร้างแต่ละบรรทัด:
 * { "ชื่อผู้พูด", "บทพูด", "รูปตัวละคร", "ฉากหลัง" }
 * { "ชื่อผู้พูด", "บทพูด", "รูปตัวละคร", "ฉากหลัง", "เสียง/Effect" }
 * 
 * ตัวเลือก (สร้าง Object[][] ต่อท้าย):
 * { "ชื่อผู้พูด", "บทพูด", "รูปตัวละคร", "ฉากหลัง",
 *   new Object[][] {
 *     { "ข้อความปุ่ม", "SCENE_TARGET", "ชื่อตัวละคร", คะแนน },
 *     { "ข้อความปุ่ม", "SCENE_TARGET" }   // ไม่มีคะแนน
 *   }
 * }
 * 
 * Effect ที่รองรับ: FADE_WHITE_OPEN, WHITE_FADE_OUT, FADE_BLACK, SHAKE, FLASH
 * เสียง: BGM_xxx (เพลงพื้นหลัง), xxx (เสียงเอฟเฟกต์)
 */
public class StoryData {

    // =========================
    // SCENE 1 { "ธีร์", "ข้อความ", "ตลค.", "ตลค.", "meetingroom.png", "none", "none" }
    // =========================
    public static final Object[][] SCENE_1 = {
        {"", "ความรักมีหลายรสชาติ", "none", "none","black.jpg", "none", "none"},
        {"", "บางครั้งหวานละมุน...", "none", "none", "black.jpg", "none", "none"},
        {"", "บางครั้งขมขื่น...", "none", "none", "black.jpg", "none", "none"},
        {"", "ในโลกใบนี้มีความรัก...", "none", "none", "black.jpg", "none", "none"},
        {"", "และเรื่องของผมก็กำลังกลายเป็นหนึ่งในนั้น...", "none", "none", "whiteroom.png", "none", "FADE_WHITE_OPEN"}
    };

    // =========================
    // SCENE 2: อารัมภบท (เจ็บแต่ละมุน)
    // =========================
    public static final Object[][] SCENE_2 = {
        {"", "เจ็ดปี…", "none", "darkroom.png", "BGM_Warm", "FADE_IN"},
        {"", "ไม่ใช่ช่วงเวลาสั้น ๆ เลย", "none", "darkroom.png", "none", "none"},
        {"", "มันยาวพอให้ผมจำได้ว่าเขาชอบกาแฟแบบไหน", "none", "darkroom.png", "none", "none"},
        {"", "ยาวพอให้ผมเผลอเรียกเขาในใจว่า “อนาคต”", "none", "darkroom.png", "none", "none"},
        
        {"", "จนวันหนึ่ง ผมเห็นเขากับผู้หญิงคนหนึ่ง", "none", "darkroom.png", "none", "FADE_TO_DARK"},
        {"", "มือของเขาวางบนแผ่นหลังเธอ…อย่างที่ไม่ควรจะทำ", "none", "darkroom.png", "none", "none"},
        {"กันย์", "เราเลิกกันเถอะ มันเปลี่ยนไปแล้ว", "Exboyfriend.png", "darkroom.png", "none", "none"},
        {"", "คำพูดนั้นมันทำให้ผมรู้สึกชาวาบไปทั้งตัว", "none", "darkroom.png", "none", "none"},
        {"", "เขาทำราวกับว่าเจ็ดปีที่ผ่านมาเป็นแค่ความทรงจำที่ไม่มีความหมายอะไรเลย", "none", "darkroom.png", "none", "none"}
    };

    // =========================
    // SCENE 3: 1 สัปดาห์ผ่านไป + เพื่อนแนะนำ
    // =========================
    public static final Object[][] SCENE_3 = {
        {"", "หนึ่งสัปดาห์หลังจากวันนั้น", "none", "office.png", "none", "FADE_IN", ""},

        {"", "ผมยังตื่นขึ้นมาในห้องเดิม แต่ความรู้สึกไม่เหมือนเดิมอีกแล้ว", "none", "bedroom.png", "none", "none"},
        {"", "ผมพยายามใช้ชีวิตตามปกติ ไปทำงาน ยิ้ม และตอบคำถามด้วยคำว่า “โอเค”", "none","office.png", "none", "none"},
        {"", "ทั้งที่ในอกเหมือนมีอะไรค้างอยู่เสมอ", "none","office.png", "none", "none"},

        {"", "เย็นวันหนึ่ง","none","sunset.png", "none", "FADE_IN", ""},

        {"", "ผมนั่งอยู่ในร้านกาแฟกับเพื่อนสนิท", "none","cafe.png", "none", "FADE_IN"},
        {"เพทาย", "บางทีการเริ่มต้นอะไรใหม่ ๆ บ้างก็ดีนะ", "Friend.png","cafe.png", "none", "none"},
        {"เพทาย", "ไม่ได้แปลว่าต้องรีบลืมใคร…แต่ฉันไม่อยากให้นายจมปลักอยู่ตรงนี้", "Friend.png","cafe.png", "none", "none"},
        {"", "คืนนั้น คำพูดของเพื่อนสะท้อนอยู่ในหัวผมทั้งคืน", "none", "bedroom.png", "none", "none"},
        {"", "ผมไม่ได้อยากมีใครแทนที่เขา…แต่ผมก็ถึงเวลาที่ผมควรจะเดินหน้าต่อไปเสียที", "none", "bedroom.png", "none", "none"}
    };

    // =========================
    // SCENE 4: แจ้งเตือน 3 คนแรก (เปิดทางไป SCENE_5)
    // =========================
    public static final Object[][] SCENE_4 = {
        {"", "00:07 น.", "none", "bedroom.png", "none", "FADE_IN"},
        {"", "ติ๊ง", "none", "bedroom.png", "SFX_Notify", "none"},
        {"", "แจ้งเตือนแรกเข้ามา", "none", "bedroom.png", "none", "none"},

        {"", "โปรไฟล์แรก…ชายในสูทสีเข้ม สายตานิ่ง สุขุม", "Profile_Thee.png", "none", "none", "none"},

        {"", "ติ๊ง", "none", "dating_chat.png", "SFX_Notify", "none"},
        {"", "โปรไฟล์ที่สอง…ชายที่รอยยิ้มสดใสราวกับแสงแดด", "KirinAlex.png.png", "dating_chat.png", "none", "none"},

        {"", "ติ๊ง", "none", "dating_chat.png", "SFX_Notify", "none"},
        {"", "โปรไฟล์ที่สาม…คำพูดเหมือน “รู้จักผม” มากกว่าที่ควรจะเป็น", "Profile_Tian.png", "none", "none", "none"},

        {"", "สามชื่อ…สามข้อความ…สามความรู้สึกที่ต่างกัน", "none", "bedroom.png", "none", "none"},
        {"", "ผมวางมือถือบนตัก ลมหายใจไม่สม่ำเสมอเล็กน้อย", "none", "bedroom.png", "none", "none"},
        {"", "ลองอะไรใหม่ ๆ บ้างก็ดี", "none", "bedroom.png", "none", "none"},

    };

    // =========================
    // SCENE 5: ฉากเลือกตอบ “ลำดับ” (ไม่ใช่รูทจีบ)
    // - เลือกแล้วไป SCENE_7/8/9 ซึ่งเป็นฉากคุยสั้นและกลับ HUB ได้
    // =========================
    public static final Object[][] SCENE_5 = {
        {"", "คืนนั้น ผมนอนมองเพดานอยู่นาน", "none", "ceilingbedroom.png", "BGM_Warm", "FADE_IN"},
        {"", "แค่การตอบข้อความ…ทำไมต้องคิดมากขนาดนี้กันนะ", "none", "ceilingbedroom.png", "none", "none"},
        {"", "ไม่เป็นไรหรอก ท่องไว้", "none", "ceilingbedroom.png", "none", "none"},
        {"", "ไม่ลองก็ไม่มีวันรู้", "none", "ceilingbedroom.png", "none", "none"},

        { "PLAYER", "ควรจะตอบใครก่อนดีนะ", "none", "ceilingbedroom.png", "none", "",
        new Object[][]{
                { "ตอบชายในสูทก่อน", "SCENE_7" },
                { "ตอบชายรอยยิ้มสดใสก่อน", "SCENE_8" },
                { "ตอบคนที่เหมือนจะรู้จักมาก่อน", "SCENE_9" }
            }
       }
    };

    // =========================
    // SCENE 6: HUB เลือกคุย (หลังจากคุยแล้วจะกลับมาที่นี่)
    // =========================
    public static final Object[][] SCENE_6 = {
        {"", "หน้ารวมแชทปรากฏขึ้นอีกครั้ง", "none", "dating_chatlist.png", "none", "FADE_IN"},
        {"", "บางทีการคุยกับคนแปลกหน้าก็ไม่ใช่เรื่องเลวร้าย", "none", "dating_chatlist.png", "none", "none"},

        { "PLAYER", "ผมจะคุยกับใครต่อดี", "none", "dating_chatlist.png", "none", "",
        new Object[][] {
                { "ธีร์", "SCENE_7" },
                { "คีริน", "SCENE_8" },
                { "เทียน", "SCENE_9" },
                { "พอแล้วไปต่อ", "SCENE_10" }
            }
        }
    };

    // =========================
    // SCENE 7: ธีร์ (คุยสั้น + กลับ HUB)
    // =========================
    public static final Object[][] SCENE_7 = {
    {"", "ผมกดเข้าแชทของชายที่ชื่อธีร์", "Teeraphat.png", "dating_chat.png", "none", "FADE_IN"},
    {"ธีร์", "สวัสดีครับ", "Teeraphat.png", "dating_chat.png", "none", "none"},
    {"PLAYER", "สวัสดีครับ", "none", "dating_chat.png", "none", "none"},
    {"PLAYER", "ผมพึ่งเคยเล่นแอพแบบนี้ครั้งแรก ถ้าชวนคุยไม่เก่งก็ขอโทษด้วยนะครับ", "none", "dating_chat.png", "none", "none"},
    {"ธีร์", "ผมก็เล่นครั้งแรกเหมือนกัน ไม่ต้องขอโทษหรอก เป็นตัวของตัวเองเถอะ", "Teeraphat.png", "dating_chat.png", "none", "none"},
    {"ธีร์", "คุณยังไม่นอนเหรอ", "Teeraphat.png", "dating_chat.png", "none", "none"},
    {"PLAYER", "ยังครับ มีเรื่องอะไรให้คิดนิดหน่อย", "none", "dating_chat.png", "none", "none"},
    {"ธีร์", "มีอะไรปรึกษาได้นะครับ", "Teeraphat.png", "dating_chat.png", "none", "none"},
    {"", "ประโยคสั้น ๆ แต่เหมือนมีคนยื่นความปลอดภัยให้แบบไม่ทันเรียกร้อง", "none", "bedroom.png", "none", "none"},

    { "PLAYER", "(แตะเพื่อกลับไปหน้ารวมแชท)", "none", "bedroom.png", "none", "",
        new Object[][] { { "(แตะ)", "SCENE_6" } }
        }
    };

    // =========================
    // SCENE 8: คีริน (คุยสั้น + กลับ HUB)
    // =========================
    public static final Object[][] SCENE_8 = {
        {"", "ผมกดเข้าแชทของชายที่ชื่อคีริน", "KirinAlex.png", "dating_chat.png", "none", "FADE_IN"},
        {"คีริน", "ใช่ พี่จริง ๆ เหรอเนี้ย...", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "คีริน? นายเล่นแอพหาคู่ด้วยเหรอ", "none", "dating_chat.png", "none", "none"},
        {"คีริน", "ผมสิ ต้องถามคำถามนี้กับพี่", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "อ่า..เรื่องมันยาวน่ะ", "none", "dating_chat.png", "none", "none"},
        {"คีริน", "เข้าใจแล้ว พี่ยังไม่นอนอีกเหรอ", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "ยังเลย", "none", "dating_chat.png", "none", "none"},
        {"คีริน", "มีอะไรเครียดรึเปล่า? ผมคุยเล่นเป็นเพื่อนได้นะ", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"", "คำพูดง่าย ๆ แต่มันทำให้ผมรู้สึกว่า…คนเราอาจจะไม่ต้องเข้มแข็งตลอดเวลาก็ได้", "none", "bedroom.png", "none", "none"},

        { "PLAYER", "(แตะเพื่อกลับไปหน้ารวมแชท)", "none", "bedroom.png", "none", "",
        new Object[][] { { "(แตะ)", "SCENE_6" } }

        }
    };

    // =========================
    // SCENE 9: เทียน (คุยสั้น + กลับ HUB)
    // =========================
    public static final Object[][] SCENE_9 = {
        {"", "ผมกดเข้าแชทของชายที่ชื่อ เทียน", "Tian.png", "dating_chat.png", "none", "FADE_IN"},
        {"PLAYER", "คุณพูดเหมือนรู้จักผมเลยนะครับ", "none", "dating_chat.png", "none", "none"},
        {"เทียน", "เทียนหลง..พอจะจำชื่อนี้ได้ไหม", "Tian.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "เทียนหลง...", "none", "dating_chat.png", "none", "none"},
        {"PLAYER", "พี่เทียนเหรอครับ? หลายปีที่ผ่านมา พี่ไปอยู่ที่ไหนมา", "none", "dating_chat.png", "none", "none"},
        {"เทียน", "พี่ต้องย้ายไปต่างประเทศน่ะ", "Tian.png", "dating_chat.png", "none", "none"},
        {"เทียน", "แต่ต่อจากนี้ไป พี่กลับมาแล้ว", "Tian.png", "dating_chat.png", "none", "none"},
        {"", "ความรู้สึกที่คุ้นเคยกำลังพัดพาความทรงจำในอดีตให้ไหลย้อนกลับคืนมา", "none", "bedroom.png", "none", "none"},

        { "PLAYER", "(แตะเพื่อกลับไปหน้ารวมแชท)", "none", "bedroom.png", "none", "",
        new Object[][] { { "(แตะ)", "SCENE_6" } }

        }
    };

    // =========================
    // SCENE 10: ไปต่อบทถัดไป
    // =========================
    public static final Object[][] SCENE_10 = {
        {"", "คืนนั้น ผมยิ้มออกมาเบา ๆ", "none", "bedroom.png", "BGM_Warm", "FADE_IN"},
        {"", "บางทีการเปิดหนังสือเล่มใหม่บ้าง ก็เป็นเรื่องที่ไม่เลว", "none", "black.jpg", "none", "FADE_OUT_TO_NEXT"}
    };

    // =========================
    // SCENE 11: ห้องประชุม - เจอ "ท่านประธาน" (ธีร์)
    // =========================
    public static final Object[][] SCENE_11 = {

        {"", "หนึ่งเดือนผ่านไป", "none", "capital.png", "none", "FADE_IN", ""},

        {"", "ห้องประชุมที่ผู้คนเริ่มเดินทยอยเข้ามา", "none", "meetingroom.png", "none", "FADE_IN"},
        {"", "พนักงานบางส่วนค่อย ๆ ลุกขึ้นยืน เมื่อฝีเท้าของใครคนหนึ่งก้าวเข้ามา", "none", "meetingroom.png", "none", "none"},
        {"STAFF", "สวัสดีครับท่านประธาน", "none", "meetingroom.png", "none", "none"},
        {"", "หน้าตาของผู้ชายคนนั้นทำให้ผมชะงักไปในทันที", "none", "meetingroom.png", "none", "none"},
        {"PLAYER", "ท่านประธาน?", "none", "meetingroom.png", "none", "none"},
        {"", "ร่างสูงสง่าในชุดสูทสีเข้มก้าวเข้ามาในห้องประชุมอย่างมั่นคง", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "บรรยากาศรอบตัวเขาเรียบนิ่ง แต่กลับทำให้ทุกสายตาเผลอจับจ้องโดยไม่รู้ตัว", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "ดวงตาคมกริบคู่นั้นกวาดมองผ่านผู้คน", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "ก่อนจะชะงักเล็กน้อยเมื่อสบเข้ากับผม", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"", "เขาหยุดชะงักเพียงเสี้ยววินาที ก่อนจะเงยหน้าขึ้นสบตาผมอีกครั้ง", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "มุมปากขยับเล็กน้อย เป็นรอยยิ้มบางเบาที่แทบไม่ทันสังเกต", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "แต่เพียงพอให้ผมรับรู้ถึงความหมายบางอย่างในสายตานั้น", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "การประชุมเริ่มต้นขึ้นท่ามกลางบรรยากาศที่กลับมาเป็นปกติราวกับไม่มีอะไรเกิดขึ้น", "none", "meetingroom.png", "none", "none"},
        {"", "ผมนั่งนิ่งอยู่ที่เดิม พยายามจดจ่อกับเอกสารตรงหน้า และหลีกเลี่ยงการมองไปทางเขา", "none", "meetingroom.png", "none", "none"},

        {"", "เมื่อการประชุมสิ้นสุดลง ผู้คนทยอยลุกออกจากห้องทีละคน", "none", "meetingroom.png", "none", "none"},
        {"", "เสียงบทสนทนาเบา ๆ ค่อย ๆ เลือนหายไปตามทางเดินด้านนอก", "none", "meetingroom.png", "none", "none"},

        {"", "ผมเก็บแฟ้มเอกสารเข้าที่อย่างเงียบ ๆ", "none", "meetingroom.png", "none", "none"},
        {"", "ก่อนที่เสียงฝีเท้าจะหยุดลงใกล้ตัวกว่าที่คิด", "none", "meetingroom.png", "none", "none"},

        {"", "ธีร์เค่อย ๆ โน้มตัวลงมาเล็กน้อย", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "ระยะใกล้พอให้เสียงของเขาได้ยินชัดเพียงคุณคนเดียว", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"ธีร์", "เจอตัวสักทีนะ", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"", "หัวใจของผมกระตุกแรงอย่างควบคุมไม่ได้", "none", "meetingroom.png", "none", "none"},

        {"PLAYER", "คุณบอกผมว่า เป็นพนักงานบริษัทธรรมดา ๆ ไม่ใช่เหรอครับ", "none", "meetingroom.png", "none", "none"},

        {"", "ก่อนที่ธีร์จะยืนตัวตรงอีกครั้ง", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "ทำสีหน้าเรียบนิ่งราวกับบทสนทนาเมื่อครู่ไม่เคยเกิดขึ้น", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"ธีร์", "ถ้าผมบอกไป จะเห็นสีหน้าแบบนี้ของคุณเหรอ", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"", "เมื่อพูดจบแล้ว เขาก็เดินออกไปทันทีด้วยสีหน้าเจ้าเล่ห์", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "แน่นอนว่าผมไม่ยอม แล้วก็รีบวิ่งตามเข้าไปในลิฟต์ได้ในสำเร็จ", "none", "elevator_inside.png", "none", "FADE_IN"},

    };


    // =========================
    // SCENE 12: ลิฟต์ - เจอเทียน + เข้าสตูดิโอเจอคีริน (เปิดตัวครบ 3)
    // =========================
    public static final Object[][] SCENE_12 = {

        {"", "ผมยืนอยู่ตรงกลางระหว่างพวกเขา", "none", "elevator_inside.png", "none", "none"},
        {"", "ซ้ายคือธีร์", "Teeraphat.png", "elevator_inside.png", "none", "none"},
        {"", "ขวาคือชายคนหนึ่งที่หน้าตาดูคุ้นเคยอย่างน่าประหลาด", "Tian.png", "elevator_inside.png", "none", "none"},
        {"", "พอมีคนอื่นอยู่ด้วย แน่นอนว่าผมไม่กล้าพูดขึ้น ทำให้บรรยากาศเงียบไปครู่หนึ่ง", "none", "elevator_inside.png", "none", "none"},

        {"", "ก่อนที่ผู้ชายคนนั้นจะพูดขึ้นด้วยน้ำเสียงนุ่มทุ้มสุภาพ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"เทียน", "ทำงานที่นี่เหรอ", "Tian.png", "elevator_inside.png", "none", "none"},
        {"PLAYER", "ค-ครับ? ผมเผลอตอบรอบไปอย่างงง ๆ", "none", "elevator_inside.png", "none", "none"},

        {"", "เขามองผมนิ่ง ๆ", "Tian.png", "elevator_inside.png", "none", "none"},
        {"", "แต่ภายในแววตาคู่นั้นกลับมีประกายบางอย่าง แล้วริมฝีปากก็คลี่ยิ้มบาง ๆ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"เทียน", "ไม่ได้เจอกันนาน ตัวก็ยังเล็กอยู่เหมือนเดิมเลยนะ", "Tian.png", "elevator_inside.png", "none", "none"},
        {"เทียน", "ลืมกันไปแล้วเหรอ ทั้ง ๆ ที่ตอนเด็กออกจะติดพี่แท้ ๆ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"", "หัวใจของผมกระตุกอีกครั้งในวันที่สองของวัน", "none", "elevator_inside.png", "none", "none"},
        {"", "ความทรงจำบางอย่างแล่นผ่านเข้ามา", "none", "flashback_house.png", "none", "FADE_TO_DARK"},
        {"", "ภาพบ้านหลังเก่า", "none", "flashback_house.png", "none", "none"},
        {"", "รั้วไม้", "none", "flashback_house.png", "none", "none"},
        {"", "เสียงหัวเราะ", "none", "flashback_house.png", "none", "none"},
        {"", "คำพูดนั้นทำให้ผมเงยหน้าขึ้นช้า ๆ", "none", "elevator_inside.png", "none", "FADE_IN"},

        {"PLAYER", "พี่…เทียน?", "none", "elevator_inside.png", "none", "none"},

        {"", "เขาหัวเราะเบา ๆ แววตาสีดำอ่อนลงในทันที", "Tian.png", "elevator_inside.png", "none", "none"},
        {"เทียน", "จำได้แล้วสินะ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"", "ก่อนที่ผมจะได้พูดอะไรมากกว่านี้", "none", "elevator_inside.png", "none", "none"},
        {"", "คุณธีร์ที่ยืนเงียบมาตลอดเอ่ยขึ้น", "Teeraphat.png", "elevator_inside.png", "none", "none"},

        {"ธีร์", "รู้จักกันเหรอ", "Teeraphat.png", "elevator_inside.png", "none", "none"},

        {"", "น้ำเสียงของเขาราบเรียบ ไม่มีแววขี้เล่นเหมือนก่อนหน้านี้ในห้องประชุม", "Teeraphat.png", "elevator_inside.png", "none", "none"},
        {"", "สายตาคมกริบจับจ้องมาทั้งผม และอดีตพี่ชายข้างบ้านทุกปฏิกิริยา", "Teeraphat.png", "elevator_inside.png", "none", "none"},

        {"", "ก่อนที่เขาจะเอ่ยตอบอย่างสบาย ๆ", "Tian.png", "elevator_inside.png", "none", "none"},
        {"เทียน", "น้องข้างบ้านสมัยเด็กน่ะ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"", "คุณธีร์พยักหน้าเบา ๆ", "Teeraphat.png", "elevator_inside.png", "none", "none"},
        {"", "แต่เขากลับมองมาทางผมนานกว่าปกติเล็กน้อย", "Teeraphat.png", "elevator_inside.png", "none", "none"},

        {"", "ประตูก็ลิฟต์ค่อย ๆ เคลื่อนเปิดออก", "none", "hallway_office.png", "none", "FADE_IN"},
        {"", "ผมยังมึนไม่หาย", "none", "hallway_office.png", "none", "none"},
        {"", "แต่ก็เดินออกมาพร้อมกับทั้งสองคน", "none", "hallway_office.png", "none", "none"},

        {"", "จู่ ๆ เสียงทีมงานเรียก", "none", "hallway_office.png", "none", "none"},
        {"พนักงาน", "เตรียมถ่ายพรีเซนเตอร์นะครับ!", "none", "hallway_office.png", "none", "none"},

        {"", "ผมถูกเรียกตัวเข้าไปประจำกล้อง", "none", "studio.png", "none", "FADE_IN"},
        {"", "พอเดินเข้าสตูดิโอ", "none", "studio.png", "none", "none"},
        {"", "เสียงสดใสดังขึ้นทันที", "none", "studio.png", "none", "none"},

        {"คีริน", "อ้าว!", "KirinAlex.png.png", "studio.png", "none", "none"},
        {"", "คีรินโบกมือแรง ๆ", "KirinAlex.png", "studio.png", "none", "none"},
        {"คีริน", "ตากล้องเป็นพี่เองเหรอครับ ดีใจจัง!", "KirinAlex.png", "studio.png", "none", "none"},

        {"PLAYER", "คีริน…?", "none", "studio.png", "none", "none"},

        {"", "เขาฉีกยิ้มกว้างที่สดใสราวกับพระอาทิตย์", "KirinAlex.png", "studio.png", "none", "none"},
        {"คีริน", "บังเอิญจังเลย", "KirinAlex.png", "studio.png", "none", "none"},

        {"", "ธีร์ที่ยืนมองภาพนั้นเงียบ ๆ ก็เลิกคิ้วขึ้นอีกครั้ง", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "คีริน นายก็รู้จักเขาเหรอ", "Teeraphat.png", "studio.png", "none", "none"},

        {"", "ชายอายุน้อยกว่าเอ่ยตอบทันที", "KirinAlex.png", "studio.png", "none", "none"},
        {"คีริน", "ใช่ รุ่นพี่ผมเอง", "KirinAlex.png", "studio.png", "none", "none"},

        {"PLAYER", "คีริน…?", "none", "studio.png", "none", "none"},

        {"", "ธีร์หันมาสบตาผม", "Teeraphat.png", "studio.png", "none", "none"},
        {"", "สายตานิ่ง", "Teeraphat.png", "studio.png", "none", "none"},
        {"", "แต่มีแววแกล้งบาง ๆ", "Teeraphat.png", "studio.png", "none", "none"},

    };

    // =========================
    // SCENE 13: กลางคืน / มือถือสั่น
    // =========================
    public static final Object[][] SCENE_13_NARRATION = {
        {"", "คืนนั้น", "none", "bedroom.png", "BGM_Warm", "FADE_IN"},
        {"", "ผมทิ้งตัวลงบนเตียง… แล้วปล่อยให้ความเหนื่อยไหลออกจากไหล่", "none", "bedroom.png", "none", "none"},
        {"", "หลังจากตอนกลางวัน… ใครจะเชื่อว่าโลกจะกลมขนาดนี้", "none", "bedroom.png", "none", "none"},
        {"", "ธีร์ — ประธานบริษัท", "none", "bedroom.png", "none", "none"},
        {"", "เทียน — พี่ข้างบ้านในอดีต", "none", "bedroom.png", "none", "none"},
        {"", "คีริน — รุ่นน้องที่บังเอิญมาเป็นพรีเซนเตอร์", "none", "bedroom.png", "none", "none"},
        {"", "ทั้งสามคนอยู่ในวันเดียวกัน… เหมือนโชคชะตาจะจงใจเกินไป", "none", "bedroom.png", "none", "none"},
        {"", "ติ๊ง", "none", "bedroom.png", "SFX_Notify", "none"},
        {"", "แจ้งเตือน 1 ข้อความ", "none", "bedroom.png", "none", "none"},
        {"", "ติ๊ง", "none", "bedroom.png", "SFX_Notify", "none"},
        {"", "แล้ว 2", "none", "bedroom.png", "none", "none"},
        {"", "ติ๊ง", "none", "bedroom.png", "SFX_Notify", "none"},
        {"", "แล้ว 3", "none", "bedroom.png", "none", "none"},
        {"", "ผมหลับตาไปหนึ่งวินาที", "none", "bedroom.png", "none", "none"},
        {"", "\u201c…เอาจริงดิ\u201d", "none", "bedroom.png", "none", "none"},
        {"", "หน้าจอขึ้นชื่อ 3 คน", "none", "dating_chatlist.png", "none", "FADE_IN"},
    };

    public static final Object[][] SCENE_13_CHAT = {
        {"ธีร์", "ถึงบ้านแล้วหรือยัง", "Teeraphat.png", "dating_chat.png", "none", "none"},
        {"เทียน", "วันนี้เหนื่อยไหม พี่เห็นหน้าดูเพลีย ๆ", "Tian.png", "dating_chat.png", "none", "none"},
        {"คีริน", "พี่กินข้าวยังครับ! วันนี้ดีใจมากเลยนะที่ได้ทำงานด้วยกัน", "KirinAlex.png.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "ผมจะตอบใครดี", "none", "bedroom.png", "none", "",
            new Object[][]{
                {"ตอบธีร์", "PICK13_TEER"},
                {"ตอบเทียน", "PICK13_TIAN"},
                {"ตอบคีริน", "PICK13_KIRIN"},
            }
        },
    };

public static final Object[][] SCENE_13 = SCENE_13_NARRATION;

    // =========================
    // SCENE 14: ตอบธีร์ (โทนนิ่ง/แกล้งนิด ๆ)
    // =========================
    public static final Object[][] SCENE_14 = {
        {"", "ผมกดเข้าแชทของธีร์", "Teeraphat.png", "dating_chat.png", "none", "FADE_IN"},
        {"PLAYER", "ถึงแล้วครับ", "none", "dating_chat.png", "none", "none"},
        {"ธีร์", "ดี", "Teeraphat.png", "dating_chat.png", "none", "none"},
        {"ธีร์", "ผมไม่ชอบให้คนของผมกลับบ้านดึก", "Teeraphat.png", "dating_chat.png", "none", "none"},
        {"", "ผมชะงักไปหนึ่งจังหวะ", "none", "bedroom.png", "none", "none"},
        {"", "…คนของผม?", "none", "bedroom.png", "none", "none"},
        {"ธีร์", "พรุ่งนี้มีงานเช้า", "Teeraphat.png", "dating_chat.png", "none", "none"},
        {"ธีร์", "อย่าตื่นสายล่ะ", "Teeraphat.png", "dating_chat.png", "none", "none"},

        { "PLAYER", "กลับไปหน้ารวมแชท", "none", "bedroom.png", "none", "",
            new Object[][] { { "กลับไป", "SCENE_17" } }
        }
    };

    // =========================
    // SCENE 15: ตอบเทียน (อบอุ่น/ตามใจ)
    // =========================
    public static final Object[][] SCENE_15 = {
        {"", "ผมกดเข้าแชทของเทียน", "Tian.png", "dating_chat.png", "none", "FADE_IN"},
        {"PLAYER", "เหนื่อยนิดหน่อยครับ", "none", "dating_chat.png", "none", "none"},
        {"PLAYER", "แต่ก็ชินแล้ว", "none", "dating_chat.png", "none", "none"},
        {"เทียน", "พักผ่อนเยอะ ๆ อย่านอนดึกมากนะ", "Tian.png", "dating_chat.png", "none", "none"},
        {"เทียน", "มีคนเป็นห่วง", "Tian.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "เป็นห่วงขนาดนี้ มาดูแลเลยไหมล่ะครับ", "none", "dating_chat.png", "none", "none"},
        {"เทียน", "อย่ามาท้าพี่", "Tian.png", "dating_chat.png", "none", "none"},
        {"เทียน", "เพราะนายรู้ดีว่า พี่จริงจังกับทุกคำพูดของนาย", "Tian.png", "dating_chat.png", "none", "none"},
        {"", "ผมพิมพ์ไปเพื่อหวังจะแกล้งเขา แต่กลับกลายเป็นว่าเป็นผมที่เสียท่าเอง", "none", "bedroom.png", "none", "none"},

        { "PLAYER", "กลับไปหน้ารวมแชท", "none", "bedroom.png", "none", "",
            new Object[][] { { "กลับไป", "SCENE_17" } }
        }
    };

    // =========================
    // SCENE 16: ตอบคีริน (หมาเด็ก/พลังบวก)
    // =========================
    public static final Object[][] SCENE_16 = {
        {"", "ผมกดเข้าแชทของคีริน", "KirinAlex.png.png", "dating_chat.png", "none", "FADE_IN"},
        {"PLAYER", "กินแล้ว นายล่ะ", "none", "dating_chat.png", "none", "none"},
        {"คีริน", "ผมยังไม่ได้กินเลย", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"คีริน", "พี่แนะนำเมนูให้ผมได้ไหม", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"", "อืม...ก๋วยเตี๋ยวเป็นไง", "none", "bedroom.png", "none", "none"},
        {"คีริน", "โอเค ผมกดสั่งแล้ว", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"", "ทำไมเชื่อฟังง่ายจัง", "none", "bedroom.png", "none", "none"},
        {"คีริน", "ผมว่าง่ายแค่กับพี่เท่านั้นแหละ", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"", "พี่รู้ไหม ตอนถ่าย…ผมมองพี่ตลอดเลย", "none", "bedroom.png", "none", "none"},
        {"คีริน", "ผมชอบตอนที่สายตาของพี่มองมาที่ผมคนเดียว", "KirinAlex.png", "dating_chat.png", "none", "none"},
        {"", "ตรงไปตรงมา…สมกับวัยรุ่นจริง ๆ", "none", "bedroom.png", "none", "none"},

        { "PLAYER", "กลับไปหน้ารวมแชท", "none", "bedroom.png", "none", "",
            new Object[][] { { "กลับไป", "SCENE_17" } }
        }
    };

    // =========================
    // SCENE 17: เช้า / ก่อนเริ่มงาน (ละมุนเวอร์ชัน) เริ่มนับใจที่สองฉากนี้
    // =========================
    public static final Object[][] SCENE_17 = {

        {"", "เช้าวันถัดมาบรรยากาศยังคงเงียบสงบ", "none", "studio.png", "none", "FADE_IN"},
        {"", "ทีมงานกำลังจัดไฟ เสียงพูดคุยเบา ๆ คลออยู่รอบห้อง", "none", "studio.png", "none", "none"},
        {"", "ผมยืนดูตารางเวลาในมือเงียบ ๆ", "none", "studio.png", "none", "none"},

        {"", "ท่านประธานเดินมุ่งตรงมายังผมแล้วหยุดลงตรงหน้าทันที", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "อีกสิบนาทีกว่าจะเริ่ม", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "ถ้าไม่รีบ ไปยืนดูมุมกล้องด้วยกันไหม", "Teeraphat.png", "studio.png", "none", "none"},
        {"", "น้ำเสียงเรียบ ๆ เหมือนชวนเรื่องงาน… แต่สายตาไม่ได้มองไปที่จอเลยแม้แต่น้อย", "none", "studio.png", "none", "none"},

        {"", "ขณะอีกฝั่งหนึ่ง เทียนก็เดินเข้ามาพอดี", "Tian.png", "studio.png", "none", "none"},
        {"เทียน", "พี่คิดว่าจะซื้อเครื่องดื่มเลี้ยงทุกคนในกองถ่าย", "Tian.png", "studio.png", "none", "none"},
        {"เทียน", "อยากไปกับพี่ไหม", "Tian.png", "studio.png", "none", "none"},
        {"", "น้ำเสียงเขานุ่มเหมือนเดิม แต่แฝงความใส่ใจอยู่เสมอ", "none", "studio.png", "none", "none"},

        {"", "ส่วนคีรินก็โผล่มาจากหลังฉาก", "KirinAlex.png", "studio.png", "none", "none"},
        {"คีริน", "พี่ครับ", "KirinAlex.png", "studio.png", "none", "none"},
        {"คีริน", "ก่อนเข้ากล้อง ผมขอซ้อมกับพี่สักรอบได้ไหม", "KirinAlex.png", "studio.png", "none", "none"},
        {"คีริน", "ผมจะได้ไม่ตื่นเต้นเกินไป", "KirinAlex.png", "studio.png", "none", "none"},

        {"", "ไม่มีใครพูดทับกัน", "none", "studio.png", "none", "none"},
        {"", "ไม่มีใครขัดอีกฝ่าย", "none", "studio.png", "none", "none"},
        {"", "มีเพียงคำชวนสั้น ๆ ที่ทั้งสามตน รอคำตอบจากผม", "none", "studio.png", "none", "none"},

        { "PLAYER", "เอ่อ ผมจะไปกับ…", "none", "studio.png", "none", "",
            new Object[][]{
            { "ตอบธีร์",  "PICK17_TEER"  },
            { "ตอบเทียน", "PICK17_TIAN" },
            { "ตอบคีริน", "PICK17_KIRIN" } }
        }
    };

    // =========================
    // SCENE 18 (TEER): ไปดูมุมกล้องกับธีร์ (นิ่ง สุขุม แกล้งนิด ๆ แล้วโอ๋ทีหลัง)
    // =========================
    public static final Object[][] SCENE_18 = {

        {"", "ผมเดินไปกับธีร์เงียบ ๆ", "Teeraphat.png", "studio.png", "none", "FADE_IN"},
        {"", "เขายืนมองจอมอนิเตอร์ เหมือนกำลังดูภาพรวมทั้งกองถ่าย", "Teeraphat.png", "studio.png", "none", "none"},

        {"ธีร์", "มุมนี้ดี", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "แต่ถ้าเลื่อนกล้องอีกนิด… จะเห็นสายตาคนพูดชัดกว่า", "Teeraphat.png", "studio.png", "none", "none"},

        {"", "ผมพยักหน้าแล้วก้มดูเฟรมตามที่เขาชี้", "none", "studio.png", "none", "none"},
        {"", "ธีร์เอียงคอเล็กน้อย เหมือนแกล้งถาม", "Teeraphat.png", "studio.png", "none", "none"},

        {"ธีร์", "เมื่อคืนคุณตอบผมช้า", "Teeraphat.png", "studio.png", "none", "none"},
        {"PLAYER", "ผมง่วง ๆ นิดหน่อยน่ะครับ", "none", "studio.png", "none", "none"},

        {"", "เขาหัวเราะเบา ๆ แทบไม่ได้ยิน", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "ถ้าแบบนั้นก็ต้องพักผ่อนเยอะ ๆ", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "ผมไม่อยากให้คนของผมป่วย โดยเฉพาะคุณ", "Teeraphat.png", "studio.png", "none", "none"},

        {"", "ก่อนเดินกลับ เขาหยุดอีกครั้ง", "none", "studio.png", "none", "none"},
        {"ธีร์", "ถ้าเหนื่อย…ก็บอกผม", "Teeraphat.png", "studio.png", "none", "none"},
        {"", "ความจริงคุณไม่ต้องทำงาน...ผมก็เลี้ยงคุณได้ทั้งชีวิต", "none", "studio.png", "none", "none"},
        {"", "เขาพูดเหมือนเล่น ๆ ทั้ง ๆ ที่สีหน้านิ่ง ๆ", "none", "studio.png", "none", "none"},
        {"", "ถ้าไม่ติดว่าอีกฝ่ายเป็นประธานบริษัท ผมคงตีหน้าหล่อ ๆ นั่นไปแล้ว", "none", "studio.png", "none", "none"},

        { "PLAYER", "(แตะเพื่อกลับไปที่กองถ่าย)", "none", "studio.png", "none", "",
            new Object[][] { { "(แตะ)", "SCENE_21" } }
        }
    };

    // =========================
    // SCENE 19 (TIAN): ไปซื้อเครื่องดื่มกับเทียน (อบอุ่น ตามใจ)
    // =========================
    public static final Object[][] SCENE_19 = {

        {"", "ผมกับเทียนเดินเครื่องข้างกันออกจากสตูดิโอ", "Tian.png", "street_day.png", "none", "FADE_IN"},
        {"", "อากาศเช้ายังไม่ร้อนมากนัก ลมพัดเบา ๆ จนเส้นผมปลิวไสวเล็กน้อย", "none", "street_day.png", "none", "none"},

        {"เทียน", "นายอยากกินอะไร", "Tian.png", "street_day.png", "none", "none"},
        {"เทียน", "กาแฟ ชา หรือนม", "Tian.png", "street_day.png", "none", "none"},
        {"PLAYER", "อะไรก็ได้ครับ…ผมตามใจคนเลี้ยง", "none", "street_day.png", "none", "none"},

        {"", "เทียนยิ้มบาง ๆ เหมือนรับคำว่า ‘ฝากไว้’ ได้อย่างเป็นธรรมชาติ", "Tian.png", "street_day.png", "none", "none"},
        {"เทียน", "โอเค งั้นเอาเป็นชาเขียวหวานน้อย กับทาร์ตไข่นะ", "Tian.png", "street_day.png", "none", "none"},
        {"", "คำพูดนั้น ทำให้ผมรู้ว่าจะผ่านมาหลายปี เขาก็ยังคงจำสิ่งที่ผมชอบได้ดี", "Tian.png", "street_day.png", "none", "none"},

        {"", "ระหว่างรอเครื่องดื่ม เขาเหลือบมองหน้าผม", "none", "cafe_counter.png", "none", "FADE_IN"},
        {"เทียน", "วันนี้ดูเงียบกว่าปกติ ไม่สบายรึเปล่า", "Tian.png", "cafe_counter.png", "none", "none"},
        {"เทียน", "ฝ่ามือหนายกขึ้นมาแตะหน้าผากของผมอย่างอ่อนโยน", "Tian.png", "cafe_counter.png", "none", "none"},
        {"PLAYER", "ผมนอนดึกนิดหน่อย พอดีว่าตัดต่อภาพ", "none", "cafe_counter.png", "none", "none"},

        {"", "เทียนไม่ได้ซักต่อทันที", "Tian.png", "cafe_counter.png", "none", "none"},
        {"", "เขาแค่พยักหน้าเหมือนเข้าใจ", "Tian.png", "cafe_counter.png", "none", "none"},
        {"เทียน", "ไม่เป็นไรนะ", "Tian.png", "cafe_counter.png", "none", "none"},
        {"เทียน", "ค่อย ๆ ไปก็ได้ พี่อยู่ตรงนี้เสมอ มีอะไรให้ช่วยก็บอกได้นะ                              ", "Tian.png", "cafe_counter.png", "none", "none"},

        {"", "คำว่า ‘อยู่ตรงนี้’ ทำให้ผมอบอุ่นทุกครั้งที่ได้ยิน", "none", "cafe_counter.png", "none", "none"},
        {"", "เราหิ้วเครื่องดื่มกลับไปที่สตูดิโอด้วยกัน", "none", "studio.png", "none", "FADE_IN"},

        { "PLAYER", "(แตะเพื่อกลับไปที่กองถ่าย)", "none", "studio.png", "none", "",
            new Object[][] { { "(แตะ)", "SCENE_21" } }
        }
    };

    // =========================
    // SCENE 20 (KIRIN): ซ้อมกับคีริน (หมาเด็ก พลังบวก)
    // =========================
    public static final Object[][] SCENE_20 = {

        {"", "ผมพาคีรินไปมุมเงียบ ๆ หลังฉาก", "KirinAlex.png", "studio_backstage.png", "none", "FADE_IN"},
        {"", "เขายืนกุมบทไว้แน่นเหมือนกลัวทำพลาด ทั้ง ๆ ที่เมื่อวานก็ทำได้ดี", "KirinAlex.png", "studio_backstage.png", "none", "none"},

        {"คีริน", "พี่ครับ…ผมกังวลจัง", "KirinAlex.png", "studio_backstage.png", "none", "none"},
        {"คีริน", "ไม่ใช่เพราะเขิน หรือกลัวนะ", "KirinAlex.png", "studio_backstage.png", "none", "none"},
        {"คีริน", "เพราะวันนี้… พี่อยู่ตรงนี้ด้วย", "KirinAlex.png", "studio_backstage.png", "none", "none"},

        {"", "คำพูดตรง ๆ แบบนั้นทำให้ผมหลุดยิ้ม", "none", "studio_backstage.png", "none", "none"},
        {"PLAYER", "ถ้าพี่อยู่ก็ยิ่งต้องทำได้ดีสิ", "none", "studio_backstage.png", "none", "none"},
        {"PLAYER", "ไม่ต้องรีบ…แค่พูดให้ชัด เวลามองกล้องนึกถึงคนที่เราชอบ", "none", "studio_backstage.png", "none", "none"},

        {"", "คีรินกลืนน้ำลาย แล้วพยักหน้าแรง ๆ", "KirinAlex.png", "studio_backstage.png", "none", "none"},
        {"คีริน", "ครับ!", "KirinAlex.png", "studio_backstage.png", "none", "none"},

        {"", "เขาซ้อมตามที่ผมบอก… และทำได้ดีตามมาตรฐานของเขา", "none", "studio_backstage.png", "none", "none"},
        {"", "พอจบ คีรินก็ยิ้มกว้างเหมือนเด็กที่เพิ่งสอบผ่าน", "KirinAlex.png", "studio_backstage.png", "none", "none"},

        {"คีริน", "เห็นไหมพี่! ผมทำได้!", "KirinAlex.png", "studio_backstage.png", "none", "none"},
        {"PLAYER", "ทำได้สิ", "none", "studio_backstage.png", "none", "none"},
        {"คีริน", "พี่บอกว่าให้นึกถึงคนที่ชอบ...พอคิดถึงพี่ใจผมก็นิ่งขึ้นมาทันทีเลย", "KirinAlex.png", "studio_backstage.png", "none", "none"},
        {"", "หึ พอสบโอกาสก็ชอบหยอกทุกทีเลยนะ ไอ่เด็กคนนี้", "none", "studio_backstage.png", "none", "none"},

        { "PLAYER", "(แตะเพื่อกลับไปที่กองถ่าย)", "none", "studio.png", "none", "",
            new Object[][] { { "(แตะ)", "SCENE_21" } }
        }
    };

    // =========================
    // SCENE 21: กลับมารวม (ละมุน) + ปิดฉากเช้า
    // =========================
    public static final Object[][] SCENE_21 = {

        {"", "บางครั้งชีวิตก็มีเรื่องที่คาดไม่ถึงมากมาย", "none", "studio.png", "none", "none"},
        {"", "เหตุการณ์ที่ไม่ได้วางแผนไว้", "none", "studio.png", "none", "none"},
        {"", "คนที่ไม่ได้คิดว่าจะได้พบอีก", "none", "studio.png", "none", "none"},
        {"", "แต่พอมองดี ๆ มันก็ไม่ได้แย่สักเท่าไหร่", "none", "studio.png", "none", "none"},
        {"", "แค่อาจจะต้องค่อย ๆ ทำความเข้าใจกับมัน", "none", "studio.png", "none", "none"},

        {"", "ผมเงยหน้ามองไฟสตูดิโอ แล้วคิดในใจ", "none", "studio.png", "none", "none"},
        {"", "โลกก็แบบนี้แหละนะ", "none", "studio.png", "none", "none"},

    };

    // =========================
    // SCENE 22: หลังเลิกงาน / คำชวนทั้งสาม 
    // =========================
    public static final Object[][] SCENE_22 = {

        {"", "วันนี้เลิกงานช้ากว่าปกติเล็กน้อย", "none", "studio_night.png", "none", "FADE_IN"},
        {"", "ไฟในสตูดิโอค่อย ๆ ดับลงทีละดวง", "none", "studio_night.png", "none", "none"},
        {"", "ผมสะพายกระเป๋า กำลังจะเดินออกจากอาคาร", "none", "studio_night.png", "none", "none"},

        {"", "“จะกลับแล้วเหรอ”", "none", "sunset.png", "none", "FADE_IN"},
        {"", "ธีร์ยืนอยู่ข้างรถ ปลดกระดุมสูทออกหนึ่งเม็ด", "Teeraphat.png", "sunset.png", "none", "none"},
        {"ธีร์", "วันนี้ทำงานดี", "Teeraphat.png", "sunset.png", "none", "none"},
        {"ธีร์", "ถ้าไม่รีบ… ไปเดินดูวิวแม่น้ำกันไหม", "Teeraphat.png", "sunset.png", "none", "none"},

        {"", "ยังไม่ทันตอบ อีกเสียงหนึ่งดังตามมา", "none", "sunset.png", "none", "none"},
        {"เทียน", "พี่ว่าจะไปตลาดกลางคืน", "Tian.png", "sunset.png", "none", "none"},
        {"เทียน", "ไม่ได้ไปมานานแล้ว", "Tian.png", "sunset.png", "none", "none"},
        {"เทียน", "ถ้าว่าง ไปเดินเล่นด้วยกันไหม", "Tian.png", "sunset.png", "none", "none"},

        {"", "ลมเย็นพัดผ่านหน้าอาคาร", "none", "sunset.png", "none", "none"},
        {"", "แล้วเสียงสดใสดังขึ้นจากด้านหลัง", "none", "sunset.png", "none", "none"},

        {"คีร", "พี่!", "KirinAlex.png", "sunset.png", "none", "none"},
        {"คีริน", "ข้างมหาวิทยาลัยมีมหกรรมงานวัดพอดี", "KirinAlex.png", "sunset.png", "none", "none"},
        {"คีริน", "มีชิงช้าสวรรค์ด้วยนะ", "KirinAlex.png", "sunset.png", "none", "none"},
        {"คีริน", "ถ้าพี่ไปด้วย… ผมจะดีใจมาก", "KirinAlex.png", "sunset.png", "none", "none"},

        {"", "สามคำชวน", "none", "sunset.png", "none", "none"},
        {"", "สามที่หมาย", "none", "sunset.png", "none", "none"},
        {"", "สามความรู้สึกที่ต่างกัน", "none", "sunset.png", "none", "none"},
        {"", "คืนนี้… ผมต้องเลือกแล้ว", "none", "sunset.png", "none", "none"},

        { "PLAYER", "เอ่อ ผมอยากไปเที่ยวกับ…", "none", "sunset.png", "none", "",
            new Object[][]{
            { "ไปกับธีร์",  "PICK22_TEER"  },
            { "ไปกับเทียน", "PICK22_TIAN" },
            { "ไปกับคีริน", "PICK22_KIRIN" }}
        }
    };

    // =========================
    // SCENE_TEER_END: Happy End (ธีร์)
    // =========================
    public static final Object[][] SCENE_TEER_END = {

        {"", "หลังเลิกงาน ไฟในสตูดิโอดับลงทีละดวง", "none", "studio_night.png", "none", "FADE_IN"},
        {"", "ทีมงานทยอยกลับ เหลือเพียงความเงียบของห้องกว้าง", "none", "studio_night.png", "none", "none"},

        {"", "ธีร์เดินเข้ามาหยุดข้างผมเหมือนเคย", "Teeraphat.png", "studio_night.png", "none", "none"},
        {"ธีร์", "วันนี้ทำได้ดีมาก", "Teeraphat.png", "studio_night.png", "none", "none"},
        {"PLAYER", "ขอบคุณครับ", "none", "studio_night.png", "none", "none"},

        {"ธีร์", "ขึ้นไปข้างบนกับผมหน่อยได้ไหม", "Teeraphat.png", "studio_night.png", "none", "none"},
        {"", "ผมยังไม่ทันถาม หรือตอบตกลง เขาก็เดินนำไปที่ลิฟต์", "none", "studio_night.png", "none", "none"},

        {"", "ประตูลิฟต์เปิดออกสู่ชั้นบนสุดของตึก", "none", "rooftop_night.png", "none", "FADE_IN"},
        {"", "ลมกลางคืนพัดเบา ๆ เมืองทั้งเมืองส่องแสงอยู่ใต้เท้าเรา", "none", "rooftop_night.png", "none", "none"},
        {"", "ไฟตึกสูงและถนนยาวไกลเหมือนดาวบนพื้นดิน", "none", "rooftop_night.png", "none", "none"},

        {"", "ธีร์ยืนมองผมเงียบ ๆ สักพัก", "Teeraphat.png", "rooftop_night.png", "none", "none"},
        {"", "ก่อนจะยกมือขึ้นลูบหัวผมเบา ๆ", "Teeraphat.png", "rooftop_night.png", "none", "none"},

        {"ธีร์", "ดูเธอสิเด็กดี หน้าแดงหมดแล้ว", "Mc_Theeraphat.jpg", "none", "none", "none"},

        {"", "ผมรีบหลบสายตา แต่หัวใจกลับเต้นแรงขึ้นทุกที", "Mc_Theeraphat.jpg", "none", "none", "none"},

        {"", "ธีร์หยิบอะไรบางอย่างออกมาจากด้านหลัง", "Teeraphat.png", "rooftop_night.png", "none", "none"},
        {"", "มันคือช่อดอกไม้ที่ผมไม่รู้ว่าเขาเตรียมไว้ตั้งแต่เมื่อไหร่", "Teeraphat.png", "rooftop_night.png", "none", "none"},

        {"ธีร์", "ผมไม่ค่อยเก่งเรื่องพูดอะไรยาว ๆ", "Teeraphat.png", "rooftop_night.png", "none", "none"},
        {"ธีร์", "แต่ผมอยากให้คุณอยู่ข้างผม… ไม่ใช่แค่ในกองถ่าย", "Teeraphat.png", "rooftop_night.png", "none", "none"},
        {"ธีร์", "แต่เป็นตลอดไปนับจากนี้", "Teeraphat.png", "rooftop_night.png", "none", "none"},

        {"", "ผมมองช่อดอกไม้ในมือเขา", "none", "rooftop_night.png", "none", "none"},
        {"", "แล้วเงยหน้าขึ้นมองคนตรงหน้าอีกครั้ง", "none", "rooftop_night.png", "none", "none"},

        {"PLAYER", "…ครับ", "none", "rooftop_night.png", "none", "none"},
        {"PLAYER", "ผมตกลง", "none", "rooftop_night.png", "none", "none"},

        {"", "— HAPPY END (ธีร์) —", "none", "TheeHappyEndding.png", "none", "FADE_OUT_TO_NEXT"}
    };


    // =========================
    // SCENE_TIAN_END: Happy End (เทียน) — Sunset Memory Version
    // =========================
    public static final Object[][] SCENE_TIAN_END = {

        {"", "หลังเลิกงาน เทียนก็เดินมาหยุดข้าง ๆ ผมเหมือนเช่นเคยทุกครั้งตลอด 1 เดือนที่ผ่านมา", "Tian.png", "hallway_office.png", "none", "FADE_IN"},
        {"เทียน", "ไปเดินเล่นกับพี่หน่อยได้ไหม", "Tian.png", "hallway_office.png", "none", "none"},
        
        {"", "คำชวนสั้น ๆ แต่เหมือนดึงผมออกจากความเหนื่อยทั้งวัน", "none", "hallway_office.png", "none", "none"},
        {"PLAYER", "ไปครับ", "none", "hallway_office.png", "none", "none"},

        {"", "พวกเราหยุดที่โถงกระจกกว้าง แสงสีส้มไหลทับตึกไกล ๆ จนเมืองดูเงียบลง", "none", "sunset_glass_view.png", "none", "FADE_IN"},
        {"", "ผมยืนมองวิว แล้วดันนึกถึงเรื่องเก่า ๆ ที่เคยคิดว่าลืมไปแล้ว", "none", "sunset_glass_view.png", "none", "none"},
        {"", "เทียนไม่ได้ถามอะไร เขาแค่ยืนอยู่ข้าง ๆ เหมือนรู้ว่าบางความทรงจำต้องใช้เวลา", "Tian.png", "sunset_glass_view.png", "none", "none"},

        {"เทียน", "เมื่อก่อน…นายก็ชอบมองฟ้าแบบนี้", "Tian.png", "sunset_glass_view.png", "none", "none"},
        {"", "ผมเผลอยิ้มบาง ๆ เหมือนโดนปลายแสงแตะความรู้สึกที่ซ่อนอยู่", "none", "sunset_glass_view.png", "none", "none"},
        {"PLAYER", "อื้ม...เพราะมองท้องฟ้าแล้วใจรู้สึกสงบ", "none", "sunset_glass_view.png", "none", "none"},

        {"", "ลมจากช่องแอร์พัดผ่าน แผ่ว ๆ แต่พอให้ผมรู้สึกเย็นที่หน้าผาก", "none", "sunset_glass_view.png", "none", "none"},
        {"", "เทียนยกมือขึ้นลูบหัวผมเบา ๆ จัดเส้นผมที่โดนลมให้เข้าที่", "Tian.png", "sunset_glass_view.png", "none", "none"},
        {"เทียน", "ลมพักแรง ผมยุ่งหมดแล้วตัวเล็ก", "Mc_Tianlong.jpg", "none", "none", "none"},

        {"", "ประโยคนั้นทำให้ผมหัวเราะออกมาเบา ๆ ทั้งที่ใจมันอุ่นแปลก ๆ", "none", "sunset_glass_view.png", "none", "none"},
        {"", "เทียนถอนหายใจเหมือนตัดสินใจ แล้วค่อย ๆ ยื่นช่อดอกไม้ที่ซ่อนไว้ข้างหลังออกมา", "Tian.png", "sunset_glass_view.png", "none", "none"},

        {"เทียน", "พี่ไม่เก่งพูดคำหวาน", "Tian.png", "sunset_glass_view.png", "none", "none"},
        {"เทียน", "แต่พี่อยากอยู่ข้างนาย…ไม่อยากจะต้องพรากจากไปไหนอีก", "Tian.png", "sunset_glass_view.png", "none", "none"},
        {"เทียน", "เป็นแฟนกับพี่ไหม", "Tian.png", "sunset_glass_view.png", "none", "none"},

        {"", "ผมมองช่อดอกไม้ แล้วเผลอมองกลับไปที่รอยยิ้มของเขา—เหมือนพระอาทิตย์กำลังตกอยู่หลังดวงตาคู่นั้น", "none", "sunset_glass_view.png", "none", "none"},
        {"PLAYER", "ครับ…ต่อจากนี้อย่าหนีผมไปไหนอีกนะ พี่เทียน", "none", "sunset_glass_view.png", "none", "none"},
        {"", "— HAPPY END (เทียน) —", "none", "TianHappyEndding.png", "none", "FADE_OUT_TO_NEXT"}
    };

    // =========================
    // SCENE_KIRIN_END: Happy End (คีริน) — หลอกมาซ้อมบทละคร
    // =========================
    public static final Object[][] SCENE_KIRIN_END = {

        {"", "เลิกงานปุ๊บ คีรินก็เดินมาดักหน้าผมเหมือนเตรียมแผนไว้แล้ว", "KirinAlex.png", "hallway_office.png", "none", "FADE_IN"},
        {"คีริน", "พี่ครับ ผมมีเรื่องจะขอช่วยหน่อย", "KirinAlex.png", "hallway_office.png", "none", "none"},
        {"PLAYER", "อะไรเหรอ", "none", "hallway_office.png", "none", "none"},
        {"คีริน", "ผมต้องซ้อมบทละครนิดหน่อย", "KirinAlex.png", "hallway_office.png", "none", "none"},
        {"คีริน", "ช่วยมาเป็นคู่ซ้อมให้ผมหน่อยได้ไหม", "KirinAlex.png", "hallway_office.png", "none", "none"},

        {"", "ผมยังไม่ทันถามรายละเอียด คีรินก็ยิ้มเหมือนคนที่มั่นใจว่าผมจะไม่ปฏิเสธ", "none", "hallway_office.png", "none", "none"},
        {"PLAYER", "ก็ได้", "none", "hallway_office.png", "none", "none"},

        {"", "พวกเรามาหยุดที่ห้องซ้อมเงียบ ๆ ไฟสีอุ่นส่องพื้นไม้โล่ง", "none", "practice_room.png", "none", "FADE_IN"},
        {"", "คีรินยื่นกระดาษบทให้ผมหนึ่งแผ่น", "KirinAlex.png", "practice_room.png", "none", "none"},

        {"คีริน", "พี่อ่านบทนางเอกนะ", "KirinAlex.png", "practice_room.png", "none", "none"},
        {"PLAYER", "เดี๋ยวนะ ทำไมพี่ต้องเป็นนางเอกล่ะ", "none", "practice_room.png", "none", "none"},
        {"คีริน", "เพราะบทมันต้องเป็นแบบนั้นไงครับ", "KirinAlex.png", "practice_room.png", "none", "none"},

        {"", "เขายืนใกล้กว่าที่ควรจะเป็นนิดหน่อย ตอนเริ่มซ้อมบท", "KirinAlex.png", "practice_room.png", "none", "none"},
        {"", "ประโยคในบทค่อย ๆ กลายเป็นเหมือนคำพูดจริงมากขึ้นทุกที", "none", "practice_room.png", "none", "none"},

        {"", "ผมเผลอสบตาเขา แล้วรีบหลบสายตา", "none", "practice_room.png", "none", "none"},
        {"", "แล้วเจ้าเด็กตัวยักษ์ก็หัวเราะเบา ๆ เหมือนถูกจับได้", "KirinAlex.png", "practice_room.png", "none", "none"},

        {"คีริน", "พี่หน้าแดงมากเลย", "KirinAlex.png", "practice_room.png", "none", "none"},
        {"คีริน", "เขินเพราะบท…", "KirinAlex.png", "practice_room.png", "none", "none"},
        {"คีริน", "หรือเขินเพราะผม", "KirinAlex.png", "practice_room.png", "none", "none"},

        {"", "ผมยังไม่ทันตอบ เขาก็ยิ้มเจ้าเล่ห์เหมือนคนที่ตั้งใจให้ผมเขินตั้งแต่แรก", "none", "practice_room.png", "none", "none"},

        {"", "คีรินดันหัวของผมให้เงยขึ้นแล้วดึงตัวผมเข้ามาใกล้จนตัวเราแนบชิดกัน", "Mc_Kirin.jpg", "none", "none", "none"},

        {"คีริน", "งั้นผมขอเปลี่ยนบทนิดหน่อยนะ", "KirinAlex.png", "practice_room.png", "none", "none"},
        {"คีริน", "พี่ช่วยมาเป็นนางเอกในชีวิตผมตลอดไปเลยได้ไหม", "KirinAlex.png", "practice_room.png", "none", "none"},

        {"", "ผมเงียบไปพักหนึ่ง เพราะเพิ่งรู้ว่าการซ้อมบทเมื่อกี้…อาจไม่ใช่การแสดงเลย", "none", "practice_room.png", "none", "none"},
        {"PLAYER", "…ถ้าอย่างนั้น", "none", "practice_room.png", "none", "none"},
        {"PLAYER", "ถ้าพี่ไม่รับบทนี้ แล้วใครจะรับบทนี้ล่ะ", "none", "practice_room.png", "none", "none"},

        {"", "คีรินยิ้มกว้างเหมือนเด็กที่เพิ่งได้ของขวัญชิ้นใหญ่", "KirinAlex.png", "practice_room.png", "none", "none"},
        {"คีริน", "งั้นตั้งแต่วันนี้", "KirinAlex.png", "practice_room.png", "none", "none"},
        {"คีริน", "พี่ก็เป็นนางเอกของผมจริง ๆ แล้วนะ", "KirinAlex.png", "practice_room.png", "none", "none"},

        {"", "— HAPPY END (คีริน) —", "none", "KirinHappyEndding.png", "none", "FADE_TO_DARK"}
    };


    // =========================
    // SCENE_TRUE_END: True Ending (1-1-1) — อยู่กับตัวเอง
    // =========================
    public static final Object[][] SCENE_TRUE_END = {

        {"", "หนึ่งสัปดาห์ผ่านไป", "none", "studio_evening.png", "BGM_Warm", "FADE_IN"},
        {"", "กองถ่ายเริ่มเก็บของ ไฟในสตูดิโอค่อย ๆ ดับลงทีละดวง", "none", "studio_evening.png", "none", "none"},
        {"", "ผมยืนมองทุกอย่างเงียบ ๆ", "none", "studio_evening.png", "none", "none"},
        {"", "ชีวิตยังเหมือนเดิม", "none", "studio_evening.png", "none", "none"},
        {"", "แต่หัวใจผมไม่วุ่นวายเหมือนก่อนแล้ว", "none", "studio_evening.png", "none", "none"},

        {"", "ธีร์ เทียน และคีริน ยังอยู่ตรงนี้เหมือนเดิม", "Trio.png", "studio_evening.png", "none", "none"},
        {"", "ไม่มีใครพูดอะไรเกี่ยวกับความสัมพันธ์เลย", "none", "studio_evening.png", "none", "none"},
        {"", "แต่ผมรู้ดี", "none", "studio_evening.png", "none", "none"},
        {"", "ความรู้สึกของทั้งสามคน… ไม่ใช่แค่ความหวังดีธรรมดา", "none", "studio_evening.png", "none", "none"},

        {"", "ผมสูดลมหายใจเข้า ก่อนจะพูดออกไปก่อน", "none", "studio_evening.png", "none", "none"},

        {"PLAYER", "ผมมีอะไรอยากบอกพวกคุณหน่อยครับ", "none", "studio_evening.png", "none", "none"},

        {"", "ทั้งสามคนหันมามองผมพร้อมกัน", "Trio.png", "studio_evening.png", "none", "none"},
        {"", "ไม่มีใครรีบพูด ไม่มีใครเร่งผม", "Trio.png", "studio_evening.png", "none", "none"},

        {"PLAYER", "ก่อนอื่นเลย… ขอบคุณนะครับ", "none", "studio_evening.png", "none", "none"},
        {"PLAYER", "สำหรับทุกอย่างที่พวกคุณทำให้ผม", "none", "studio_evening.png", "none", "none"},
        {"PLAYER", "ผมรู้ว่าความปรารถนาดีของพวกคุณมันจริงใจมาก", "none", "studio_evening.png", "none", "none"},

        {"", "ผมหยุดพูดไปครู่หนึ่ง", "none", "studio_evening.png", "none", "none"},
        {"", "ก่อนจะยิ้มบาง ๆ", "none", "studio_evening.png", "none", "none"},

        {"PLAYER", "แต่ตอนนี้…", "none", "studio_evening.png", "none", "none"},
        {"PLAYER", "ผมรู้สึกว่าการได้อยู่กับตัวเองแบบนี้", "none", "studio_evening.png", "none", "none"},
        {"PLAYER", "มันทำให้ผมหายใจได้สบายที่สุด", "none", "studio_evening.png", "none", "none"},

        {"PLAYER", "ผมยังไม่อยากรีบไปไหนกับความสัมพันธ์", "none", "studio_evening.png", "none", "none"},
        {"PLAYER", "ผมแค่… อยากใช้เวลากับตัวเอง โฟกัสกับตัวเอง", "none", "studio_evening.png", "none", "none"},

        {"PLAYER", "แต่ผมก็ยังอยากมีพวกคุณอยู่ในชีวิต", "none", "studio_evening.png", "none", "none"},
        {"PLAYER", "ในฐานะเพื่อน", "none", "studio_evening.png", "none", "none"},

        {"", "ความเงียบเกิดขึ้นครู่หนึ่ง", "none", "studio_evening.png", "none", "none"},

        {"TEER", "เข้าใจ", "Teeraphat.png", "studio_evening.png", "none", "none"},
        {"TEER", "คุณเลือกสิ่งที่ตัวเองต้องการก็พอ", "Teeraphat.png", "studio_evening.png", "none", "none"},

        {"TIAN", "แบบไหนก็ได้ทั้งนั้น", "Tian.png", "studio_evening.png", "none", "none"},
        {"TIAN", "ขอแค่นายสบายใจก็พอ", "Tian.png", "studio_evening.png", "none", "none"},

        {"คีริน", "งั้นผมจะเป็นเพื่อนที่อยู่ข้างพี่ไปก่อนก็ได้!", "KirinAlex.png", "studio_evening.png", "none", "none"},

        {"", "ผมหัวเราะเบา ๆ", "none", "studio_evening.png", "none", "none"},
        {"", "ความเงียบในห้องไม่ได้อึดอัดเลยแม้แต่น้อย", "none", "studio_evening.png", "none", "none"},
        {"", "ตรงกันข้าม", "none", "studio_evening.png", "none", "none"},
        {"", "มันสงบกว่าที่เคย", "none", "studio_evening.png", "none", "none"},

        {"", "เพราะครั้งนี้", "none", "studio_evening.png", "none", "none"},
        {"", "ผมไม่ได้วิ่งหนีหัวใจตัวเองอีกแล้ว", "none", "studio_evening.png", "none", "none"},

        {"", "— TRUE END (อยู่กับตัวเอง) —", "none", "black.jpg", "none", "FADE_TO_DARK"}
    };
}