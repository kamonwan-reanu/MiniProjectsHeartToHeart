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
        {"", "เจ็ดปี…", "none", "black.jpg", "BGM_Warm", "FADE_IN"},
        {"", "ไม่ใช่ช่วงเวลาสั้น ๆ เลย", "none", "black.jpg", "none", "none"},
        {"", "มันยาวพอให้ผมจำได้ว่าเขาชอบกาแฟแบบไหน", "none", "black.jpg", "none", "none"},
        {"", "ยาวพอให้ผมเผลอเรียกเขาในใจว่า “อนาคต”", "none", "black.jpg", "none", "none"},
        
        {"", "จนวันหนึ่ง ผมเห็นเขากับผู้หญิงคนหนึ่ง", "none", "darkroom.png", "none", "FADE_TO_DARK"},
        {"", "มือของเขาวางบนแผ่นหลังเธอ…อย่างที่ไม่ควรจะทำ", "none", "none", "none", "none"},
        {"กันย์", "เราเลิกกันเถอะ มันเปลี่ยนไปแล้ว", "Exboyfriend.png", "none", "none", "none"},
        {"", "คำพูดนั้นมันทำให้ผมรู้สึกชาวาบไปทั้งตัว", "none", "none", "none", "none"},
        {"", "เขาทำราวกับว่าเจ็ดปีที่ผ่านมาเป็นแค่ความทรงจำที่ไม่มีความหมายอะไรเลย", "none", "none", "none", "none"}
    };

    // =========================
    // SCENE 3: 1 สัปดาห์ผ่านไป + เพื่อนแนะนำ
    // =========================
    public static final Object[][] SCENE_3 = {
        {"", "หนึ่งสัปดาห์หลังจากวันนั้น", "none", "office.png", "none", "FADE_IN", "CAPTION"},
        {"", "ผมยังตื่นขึ้นมาในห้องเดิม แต่ความรู้สึกไม่เหมือนเดิมอีกแล้ว", "none", "bedroom.png", "none", "none"},
        {"", "ผมพยายามใช้ชีวิตตามปกติ ไปทำงาน ยิ้ม และตอบคำถามด้วยคำว่า “โอเค”", "none","office.png", "none", "none"},
        {"", "ทั้งที่ในอกเหมือนมีอะไรค้างอยู่เสมอ", "none","office.png", "none", "none"},

        {"", "เย็นวันหนึ่ง","none","cafe.png", "none", "FADE_IN", "CAPTION"},
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
        {"", "โปรไฟล์ที่สอง…ชายที่รอยยิ้มสดใสราวกับแสงแดด", "Kirin.png", "dating_chat.png", "none", "none"},

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
        {"", "ผมกดเข้าแชทของชายที่ชื่อคีริน", "Kirin.png", "dating_chat.png", "none", "FADE_IN"},
        {"คีริน", "ใช่ พี่จริง ๆ เหรอเนี้ย...", "Kirin.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "คีริน? นายเล่นแอพหาคู่ด้วยเหรอ", "none", "dating_chat.png", "none", "none"},
        {"คีริน", "ผมสิ ต้องถามคำถามนี้กับพี่", "Kirin.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "อ่า..เรื่องมันยาวน่ะ", "none", "dating_chat.png", "none", "none"},
        {"คีริน", "เข้าใจแล้ว พี่ยังไม่นอนอีกเหรอ", "Kirin.png", "dating_chat.png", "none", "none"},
        {"PLAYER", "ยังเลย", "none", "dating_chat.png", "none", "none"},
        {"คีริน", "มีอะไรเครียดรึเปล่า? ผมคุยเล่นเป็นเพื่อนได้นะ", "Kirin.png", "dating_chat.png", "none", "none"},
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

        {"", "หนึ่งเดือนผ่านไป", "none", "meetingroom.png", "none", "FADE_IN", "CAPTION"},

        {"", "ห้องประชุมเงียบ ๆ", "none", "meetingroom.png", "none", "FADE_IN"},
        {"", "ทีมงานลุกขึ้นยืน", "none", "meetingroom.png", "none", "none"},
        {"STAFF", "สวัสดีครับท่านประธาน", "none", "meetingroom.png", "none", "none"},
        {"", "คุณชะงัก", "none", "meetingroom.png", "none", "none"},
        {"PLAYER", "ประธาน?", "none", "meetingroom.png", "none", "none"},
        {"", "ร่างสูงสง่าในชุดสูทสีเข้มก้าวเข้ามาในห้องประชุมอย่างมั่นคง", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "บรรยากาศรอบตัวเขาเรียบนิ่ง แต่กลับทำให้ทุกสายตาเผลอจับจ้องโดยไม่รู้ตัว", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "ดวงตาคมเย็นคู่นั้นกวาดมองผ่านผู้คน", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "ก่อนจะชะงักเล็กน้อยเมื่อสบเข้ากับผม", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"", "เขาหยุดชะงักเพียงเสี้ยววินาที ก่อนจะเงยหน้าขึ้นสบตาคุณอีกครั้ง", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "มุมปากขยับเล็กน้อย เป็นรอยยิ้มบางที่แทบไม่ทันสังเกต", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "ไม่กว้าง ไม่ชัดเจน", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "แต่เพียงพอให้ผมรับรู้ถึงความหมายบางอย่างในสายตานั้น", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "การประชุมเริ่มต้นขึ้นท่ามกลางบรรยากาศที่กลับมาเป็นปกติราวกับไม่มีอะไรเกิดขึ้น", "none", "meetingroom.png", "none", "none"},
        {"", "ผมนั่งนิ่งอยู่ที่เดิม พยายามจดจ่อกับเอกสารตรงหน้า และหลีกเลี่ยงการมองไปทางเขา", "none", "meetingroom.png", "none", "none"},

        {"", "เมื่อการประชุมสิ้นสุดลง ผู้คนทยอยลุกออกจากห้องทีละคน", "none", "meetingroom.png", "none", "none"},
        {"", "เสียงบทสนทนาเบา ๆ ค่อย ๆ เลือนหายไปตามทางเดินด้านนอก", "none", "meetingroom.png", "none", "none"},

        {"", "คุณเก็บแฟ้มเอกสารเข้าที่อย่างเงียบ ๆ", "none", "meetingroom.png", "none", "none"},
        {"", "ก่อนที่เสียงฝีเท้าจะหยุดลงใกล้ตัวกว่าที่คิด", "none", "meetingroom.png", "none", "none"},

        {"", "ธีร์เอนตัวลงเล็กน้อย", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "ระยะใกล้พอให้เสียงของเขาได้ยินชัดเพียงคุณคนเดียว", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"ธีร์", "เจอตัวสักทีนะ", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"", "หัวใจของผมกระตุกแรงอย่างควบคุมไม่ได้", "none", "meetingroom.png", "none", "none"},

        {"PLAYER", "คุณบอกผมว่า เป็นพนักงานบริษัทธรรมดา ๆ ไม่ใช่เหรอครับ", "none", "meetingroom.png", "none", "none"},

        {"", "ก่อนที่ธีร์จะยืนตัวตรงอีกครั้ง", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "สีหน้าเรียบนิ่งราวกับบทสนทนาเมื่อครู่ไม่เคยเกิดขึ้น", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"ธีร์", "ถ้าผมบอกไป จะเห็นสีหน้าแบบนี้ของคุณเหรอ", "Teeraphat.png", "meetingroom.png", "none", "none"},

        {"", "พูดจบแล้ว เขาก็เดินออกไปทันทีด้วยสีหน้าเจ้าเล่ห์", "Teeraphat.png", "meetingroom.png", "none", "none"},
        {"", "แน่นอนว่าผมไม่ยอม แล้วก็รีบวิ่งตามเข้าไปในลิฟต์ได้ในสำเร็จ", "none", "elevator_inside.png", "none", "FADE_IN"},

    };


    // =========================
    // SCENE 12: ลิฟต์ - เจอเทียน + เข้าสตูดิโอเจอคีริน (เปิดตัวครบ 3)
    // =========================
    public static final Object[][] SCENE_12 = {

        {"", "ผมยืนอยู่ตรงกลางระหว่างพวกเขา", "none", "elevator_inside.png", "none", "none"},
        {"", "ซ้ายคือธีร์", "Teeraphat.png", "elevator_inside.png", "none", "none"},
        {"", "ขวาคือชายคนหนึ่งที่หน้าตาดูคุ้นเคยอย่างน่าประหลาด", "none", "elevator_inside.png", "none", "none"},
        {"", "พอมีคนอื่นอยู่ด้วย แน่นอนว่าผมไม่กล้าพูดขึ้น ทำให้บรรยากาศเงียบไปครู่หนึ่ง", "none", "elevator_inside.png", "none", "none"},

        {"", "ก่อนที่ผู้ชายคนนั้นจะพูดขึ้นด้วยน้ำเสียงนุ่มทุ้มสุภาพ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"เทียน", "ทำงานที่นี่เหรอ", "Tian.png", "elevator_inside.png", "none", "none"},
        {"PLAYER", "ค-ครับ? ผมเผลอตอบรอบไปอย่างงง ๆ", "none", "elevator_inside.png", "none", "none"},

        {"", "เขามองผมนิ่ง ๆ", "Tian.png", "elevator_inside.png", "none", "none"},
        {"", "เหมือนพยายามจะอ่านสีหน้าอะไรบางอย่าง ก่อนจะยิ้มบาง ๆ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"เทียน", "ไม่ได้เจอกันนาน ตัวก็ยังเล็กอยู่เหมือนเดิมเลยนะ", "Tian.png", "elevator_inside.png", "none", "none"},
        {"เทียน", "ลืมกันไปแล้วเหรอ ทั้ง ๆ ที่ตอนเด็กออกจะติดพี่แท้ ๆ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"", "หัวใจของผมกระตุกวูบในทันที", "none", "elevator_inside.png", "none", "none"},
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
        {"", "สายตาคมกริบจับจ้องมาทั้งผม และพี่เทียนทุกปฏิกิริยา", "Teeraphat.png", "elevator_inside.png", "none", "none"},

        {"", "ก่อนที่เทียนจะเอ่ยตอบอย่างสบาย ๆ", "Tian.png", "elevator_inside.png", "none", "none"},
        {"เทียน", "น้องข้างบ้านสมัยเด็กน่ะ", "Tian.png", "elevator_inside.png", "none", "none"},

        {"", "คุณธีร์พยักหน้าเบา ๆ", "Teeraphat.png", "elevator_inside.png", "none", "none"},
        {"", "แต่เขากลับมองมาทางผมนิ่งกว่าปกติเล็กน้อย", "Teeraphat.png", "elevator_inside.png", "none", "none"},

        {"", "ปกติลิฟต์ค่อย ๆ เคลื่อนเปิดออก", "none", "hallway_office.png", "none", "FADE_IN"},
        {"", "ผมยังมึนไม่หาย", "none", "hallway_office.png", "none", "none"},
        {"", "ก่อนจะก้าวเท้าเดินออกมาพร้อมสองคน", "none", "hallway_office.png", "none", "none"},

        {"", "จู่ ๆ เสียงทีมงานเรียก", "none", "hallway_office.png", "none", "none"},
        {"STAFF", "เตรียมถ่ายพรีเซนเตอร์นะครับ!", "none", "hallway_office.png", "none", "none"},

        {"", "คุณถูกเรียกตัว", "none", "studio.png", "none", "FADE_IN"},
        {"", "พอเดินเข้าสตูดิโอ", "none", "studio.png", "none", "none"},
        {"", "เสียงสดใสดังขึ้นทันที", "none", "studio.png", "none", "none"},

        {"คีริน", "อ้าว!", "Kirin.png", "studio.png", "none", "none"},
        {"", "คีรินโบกมือแรง ๆ", "Kirin.png", "studio.png", "none", "none"},
        {"คีริน", "ตากล้องเป็นพี่เองเหรอครับ ดีใจจัง!", "Kirin.png", "studio.png", "none", "none"},

        {"PLAYER", "คีริน…?", "none", "studio.png", "none", "none"},

        {"", "เขายิ้มกว้าง", "Kirin.png", "studio.png", "none", "none"},
        {"คีริน", "บังเอิญจังเลย", "Kirin.png", "studio.png", "none", "none"},

        {"", "ธีร์ที่ยืนมองภาพนั้นเงียบ ๆ เลิกคิ้วขึ้นเล็กน้อย", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "คีริน นายก็รู้จักเขาเหรอ", "Teeraphat.png", "studio.png", "none", "none"},

        {"", "คีรินตอบทันที", "Kirin.png", "studio.png", "none", "none"},
        {"คีริน", "ใช่ รุ่นพี่ผมเอง", "Kirin.png", "studio.png", "none", "none"},

        {"", "ธีร์หันมาสบตาคุณ", "Teeraphat.png", "studio.png", "none", "none"},
        {"", "สายตานิ่ง", "Teeraphat.png", "studio.png", "none", "none"},
        {"", "แต่มีแววแกล้งบาง ๆ", "Teeraphat.png", "studio.png", "none", "none"},

    };

    // =========================
    // SCENE 13: กลางคืน / มือถือสั่น
    // =========================
    public static final Object[][] SCENE_13_NARRATION = {
        {"", "คืนนั้น ห้องเงียบมาก", "none", "bedroom.png", "BGM_Warm", "FADE_IN"},
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
        {"คีริน", "พี่กินข้าวยังครับ! วันนี้ดีใจมากเลยนะที่ได้ทำงานด้วยกัน", "Kirin.png", "dating_chat.png", "none", "none"},
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
        {"ธีร์", "พรุ่งนี้มีประชุมเช้า", "Teeraphat.png", "dating_chat.png", "none", "none"},
        {"ธีร์", "อย่าตื่นสายล่ะ", "Teeraphat.png", "dating_chat.png", "none", "none"},
        {"", "คำสั้น ๆ แต่เหมือนมีอะไรพาดผ่านหัวใจเบา ๆ", "none", "bedroom.png", "none", "none"},

        { "PLAYER", "กลับไปหน้ารวมแชท", "none", "bedroom.png", "none", "",
            new Object[][] { { "กลับไป", "SCENE_17" } }
        }
    };

    // =========================
    // SCENE 15: ตอบเทียน (อบอุ่น/ตามใจ)
    // =========================
    public static final Object[][] SCENE_15 = {
        {"", "ผมกดเข้าแชทของเทียน", "Tian.png", "dating_chat.png", "none", "FADE_IN"},
        {"PLAYER", "ยังไม่นอนครับ", "none", "dating_chat.png", "none", "none"},
        {"TIAN", "ดีแล้ว", "Tian.png", "dating_chat.png", "none", "none"},
        {"TIAN", "พี่กำลังจะชงชาพอดี", "Tian.png", "dating_chat.png", "none", "none"},
        {"TIAN", "ถ้าอยู่ใกล้กันคงชวนมาด้วย", "Tian.png", "dating_chat.png", "none", "none"},
        {"", "ผมเผลอยิ้มโดยไม่รู้ตัว", "none", "bedroom.png", "none", "none"},
        {"TIAN", "ไม่ต้องรีบตอบใครหรอก", "Tian.png", "dating_chat.png", "none", "none"},
        {"TIAN", "คิดดี ๆ ก่อน", "Tian.png", "dating_chat.png", "none", "none"},
        {"", "ประโยคสุดท้าย… ทำให้ใจผมสั่นนิด ๆ", "none", "bedroom.png", "none", "none"},

        { "PLAYER", "กลับไปหน้ารวมแชท", "none", "bedroom.png", "none", "",
            new Object[][] { { "กลับไป", "SCENE_17" } }
        }
    };

    // =========================
    // SCENE 16: ตอบคีริน (หมาเด็ก/พลังบวก)
    // =========================
    public static final Object[][] SCENE_16 = {
        {"", "ผมกดเข้าแชทของคีริน", "Kirin.png", "dating_chat.png", "none", "FADE_IN"},
        {"PLAYER", "กินแล้ว นายล่ะ", "none", "dating_chat.png", "none", "none"},
        {"KIRIN", "ผมยังเลย!", "Kirin.png", "dating_chat.png", "none", "none"},
        {"KIRIN", "แต่วันนี้มีแรงเยอะมาก", "Kirin.png", "dating_chat.png", "none", "none"},
        {"KIRIN", "พี่รู้ไหม ตอนถ่าย… ผมมองพี่ตลอดเลย", "Kirin.png", "dating_chat.png", "none", "none"},
        {"", "ผมนิ่งไปครู่หนึ่ง", "none", "bedroom.png", "none", "none"},
        {"KIRIN", "ผมชอบตอนที่สายตาของพี่มองมาที่ผมคนเดียว", "Kirin.png", "dating_chat.png", "none", "none"},
        {"", "ตรงไปตรงมา…สมกับวัยรุ่นจริง ๆ", "none", "bedroom.png", "none", "none"},

        { "PLAYER", "กลับไปหน้ารวมแชท", "none", "bedroom.png", "none", "",
            new Object[][] { { "กลับไป", "SCENE_17" } }
        }
    };

    // =========================
    // SCENE 17: เช้า / ก่อนเริ่มงาน (ละมุนเวอร์ชัน) เริ่มนับใจที่สองฉากนี้
    // =========================
    public static final Object[][] SCENE_17 = {

        {"", "เช้าวันถัดมา งานยังไม่เริ่มทันที", "none", "studio.png", "none", "FADE_IN"},
        {"", "ทีมงานกำลังจัดไฟ เสียงพูดคุยเบา ๆ คลออยู่รอบห้อง", "none", "studio.png", "none", "none"},
        {"", "ผมยืนดูตารางเวลาในมือเงียบ ๆ", "none", "studio.png", "none", "none"},

        {"", "ธีร์เดินเข้ามาใกล้พอดี", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "อีกสิบนาทีกว่าจะเริ่ม", "Teeraphat.png", "studio.png", "none", "none"},
        {"ธีร์", "ถ้าไม่รีบ ไปยืนดูมุมกล้องด้วยกันไหม", "Teeraphat.png", "studio.png", "none", "none"},
        {"", "น้ำเสียงเรียบ ๆ เหมือนชวนเรื่องงาน… แต่สายตาไม่ได้มองไปที่จอเลยแม้แต่น้อย", "none", "studio.png", "none", "none"},

        {"", "อีกฝั่งหนึ่ง เทียนเดินเข้ามาพอดี", "Tian.png", "studio.png", "none", "none"},
        {"เทียน", "พี่คิดว่าจะซื้อเครื่องดื่มเลี้ยงทุกคนในกองถ่าย", "Tian.png", "studio.png", "none", "none"},
        {"เทียน", "อยากไปกับพี่ไหม", "Tian.png", "studio.png", "none", "none"},
        {"", "น้ำเสียงเขานุ่มเหมือนเดิม แต่แฝงความใส่ใจที่ไม่ต้องประกาศเสียงดัง", "none", "studio.png", "none", "none"},

        {"", "คีรินโผล่มาจากหลังฉาก", "Kirin.png", "studio.png", "none", "none"},
        {"KIRIN", "พี่ครับ", "Kirin.png", "studio.png", "none", "none"},
        {"KIRIN", "ก่อนเข้ากล้อง ผมขอซ้อมกับพี่สักรอบได้ไหม", "Kirin.png", "studio.png", "none", "none"},
        {"KIRIN", "ผมจะได้ไม่ตื่นเต้นเกินไป", "Kirin.png", "studio.png", "none", "none"},

        {"", "ไม่มีใครพูดทับกัน", "none", "studio.png", "none", "none"},
        {"", "ไม่มีใครขัดอีกคน", "none", "studio.png", "none", "none"},
        {"", "แค่สามคำชวน ที่รอคำตอบจากผม", "none", "studio.png", "none", "none"},

        { "PLAYER", "ผมจะไปกับ…", "none", "studio.png", "none", "",
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

        {"", "ประโยคนั้นไม่ได้ดัง… แต่เหมือนวางมือลงบนหัวใจผมเบา ๆ", "none", "studio.png", "none", "none"},
        {"", "ก่อนเดินกลับ เขาหยุดอีกครั้ง", "Teeraphat.png", "studio.png", "none", "none"},

        {"ธีร์", "ถ้าเหนื่อย… บอกได้", "Teeraphat.png", "studio.png", "none", "none"},
        {"", "แกล้งนิด ๆ แล้วโอ๋ทีหลัง… ตามสไตล์เขาไม่มีผิด", "none", "studio.png", "none", "none"},

        { "PLAYER", "(แตะเพื่อกลับไปที่กองถ่าย)", "none", "studio.png", "none", "",
            new Object[][] { { "(แตะ)", "SCENE_21" } }
        }
    };

    // =========================
    // SCENE 19 (TIAN): ไปซื้อเครื่องดื่มกับเทียน (อบอุ่น ตามใจ)
    // =========================
    public static final Object[][] SCENE_19 = {

        {"", "ผมเดินไปกับเทียนออกจากสตูดิโอ", "Tian.png", "street_day.png", "none", "FADE_IN"},
        {"", "อากาศเช้ายังไม่ร้อนมาก ลมพัดเบา ๆ", "none", "street_day.png", "none", "none"},

        {"TIAN", "อยากได้อะไรไหม", "Tian.png", "street_day.png", "none", "none"},
        {"TIAN", "กาแฟ ชา หรือหวานน้อย ๆ", "Tian.png", "street_day.png", "none", "none"},
        {"PLAYER", "อะไรก็ได้ครับ… เอาตามที่พี่เห็นว่าเหมาะก็พอ", "none", "street_day.png", "none", "none"},

        {"", "เทียนยิ้มบาง ๆ เหมือนรับคำว่า ‘ฝากไว้’ ได้อย่างเป็นธรรมชาติ", "Tian.png", "street_day.png", "none", "none"},
        {"TIAN", "โอเค งั้นเอาเป็นชาเขียวหวานน้อยนะ", "Tian.png", "street_day.png", "none", "none"},
        {"", "จะผ่านมาหลายปี เขาก็ยังคงจำสิ่งที่ผมชอบได้ดี", "Tian.png", "street_day.png", "none", "none"},

        {"", "ระหว่างรอเครื่องดื่ม เขาเหลือบมองหน้าผม", "none", "cafe_counter.png", "none", "FADE_IN"},
        {"TIAN", "วันนี้ดูเงียบกว่าปกติ ไม่สบายรึเปล่า", "Tian.png", "cafe_counter.png", "none", "none"},
        {"PLAYER", "ผมแค่…ยังปรับตัวไม่ค่อยทันครับ", "none", "cafe_counter.png", "none", "none"},

        {"", "เทียนไม่ได้ซักต่อทันที", "Tian.png", "cafe_counter.png", "none", "none"},
        {"", "เขาแค่พยักหน้าเหมือนเข้าใจ", "Tian.png", "cafe_counter.png", "none", "none"},
        {"TIAN", "ไม่เป็นไร", "Tian.png", "cafe_counter.png", "none", "none"},
        {"TIAN", "ค่อย ๆ ไปก็ได้ พี่อยู่ตรงนี้", "Tian.png", "cafe_counter.png", "none", "none"},

        {"", "คำว่า ‘อยู่ตรงนี้’ ทำให้ผมเผลอหายใจโล่งขึ้นจริง ๆ", "none", "cafe_counter.png", "none", "none"},
        {"", "เราหิ้วเครื่องดื่มกลับไปที่สตูดิโอด้วยกัน", "none", "studio.png", "none", "FADE_IN"},

        { "PLAYER", "(แตะเพื่อกลับไปที่กองถ่าย)", "none", "studio.png", "none", "",
            new Object[][] { { "(แตะ)", "SCENE_21" } }
        }
    };

    // =========================
    // SCENE 20 (KIRIN): ซ้อมกับคีริน (หมาเด็ก พลังบวก)
    // =========================
    public static final Object[][] SCENE_20 = {

        {"", "ผมพาคีรินไปมุมเงียบ ๆ หลังฉาก", "Kirin.png", "studio_backstage.png", "none", "FADE_IN"},
        {"", "เขายืนกุมบทไว้แน่นเหมือนกลัวทำพลาด", "Kirin.png", "studio_backstage.png", "none", "none"},

        {"KIRIN", "พี่ครับ…ผมไม่อยากเสียงาน", "Kirin.png", "studio_backstage.png", "none", "none"},
        {"KIRIN", "ไม่ใช่เพราะเงินนะ", "Kirin.png", "studio_backstage.png", "none", "none"},
        {"KIRIN", "เพราะวันนี้… พี่อยู่ตรงนี้ด้วย", "Kirin.png", "studio_backstage.png", "none", "none"},

        {"", "คำพูดตรง ๆ แบบนั้นทำให้ผมหลุดยิ้ม", "none", "studio_backstage.png", "none", "none"},
        {"PLAYER", "ถ้าพี่อยู่ก็ยิ่งต้องทำได้ดีสิ", "none", "studio_backstage.png", "none", "none"},
        {"PLAYER", "ไม่ต้องรีบ… แค่พูดให้ชัด แล้วมองกล้องเหมือนมองคนที่ไว้ใจ", "none", "studio_backstage.png", "none", "none"},

        {"", "คีรินกลืนน้ำลาย แล้วพยักหน้าแรง ๆ", "Kirin.png", "studio_backstage.png", "none", "none"},
        {"KIRIN", "ครับ!", "Kirin.png", "studio_backstage.png", "none", "none"},

        {"", "เขาซ้อมตามที่ผมบอก… และทำได้ดีตามมาตรฐานของเขา", "none", "studio_backstage.png", "none", "none"},
        {"", "พอจบ คีรินก็ยิ้มกว้างเหมือนเด็กที่เพิ่งสอบผ่าน", "Kirin.png", "studio_backstage.png", "none", "none"},

        {"KIRIN", "เห็นไหมพี่! ผมทำได้!", "Kirin.png", "studio_backstage.png", "none", "none"},
        {"PLAYER", "ทำได้สิ", "none", "studio_backstage.png", "none", "none"},
        {"KIRIN", "งั้น… หลังเลิกงาน พี่อย่าหายไปไหนนะครับ", "Kirin.png", "studio_backstage.png", "none", "none"},
        {"", "พลังบวกของเขาเหมือนดันหัวใจผมให้ลุกขึ้นยืนอีกครั้ง", "none", "studio_backstage.png", "none", "none"},

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
    // SCENE 22: หลังเลิกงาน / คำชวนทั้งสาม (งานวัด)
    // =========================
    public static final Object[][] SCENE_22 = {

        {"", "เลิกงานช้ากว่าปกติเล็กน้อย", "none", "studio_night.png", "none", "FADE_IN"},
        {"", "ไฟในสตูดิโอค่อย ๆ ดับลงทีละดวง", "none", "studio_night.png", "none", "none"},
        {"", "ผมสะพายกระเป๋า กำลังจะเดินออกจากอาคาร", "none", "studio_night.png", "none", "none"},

        {"", "“จะกลับแล้วเหรอ”", "none", "studio_front.png", "none", "FADE_IN"},
        {"", "ธีร์ยืนอยู่ข้างรถ ปลดกระดุมสูทออกหนึ่งเม็ด", "Teeraphat.png", "studio_front.png", "none", "none"},
        {"ธีร์", "วันนี้ทำงานดี", "Teeraphat.png", "studio_front.png", "none", "none"},
        {"ธีร์", "ถ้าไม่รีบ… ไปเดินดูวิวแม่น้ำกันไหม", "Teeraphat.png", "studio_front.png", "none", "none"},

        {"", "ยังไม่ทันตอบ อีกเสียงหนึ่งดังตามมา", "none", "studio_front.png", "none", "none"},
        {"เทียน", "พี่ว่าจะไปตลาดกลางคืน", "Tian.png", "studio_front.png", "none", "none"},
        {"เทียน", "ไม่ได้ไปมานานแล้ว", "Tian.png", "studio_front.png", "none", "none"},
        {"เทียน", "ถ้าว่าง ไปเดินเล่นด้วยกันไหม", "Tian.png", "studio_front.png", "none", "none"},

        {"", "ลมเย็นพัดผ่านหน้าอาคาร", "none", "studio_front.png", "none", "none"},
        {"", "แล้วเสียงสดใสดังขึ้นจากด้านหลัง", "none", "studio_front.png", "none", "none"},

        {"คีร", "พี่!", "Kirin.png", "studio_front.png", "none", "none"},
        {"คีริน", "ข้างมหาวิทยาลัยมีมหกรรมงานวัดพอดี", "Kirin.png", "studio_front.png", "none", "none"},
        {"คีริน", "มีชิงช้าสวรรค์ด้วยนะ", "Kirin.png", "studio_front.png", "none", "none"},
        {"คีริน", "ถ้าพี่ไปด้วย… ผมจะดีใจมาก", "Kirin.png", "studio_front.png", "none", "none"},

        {"", "สามคำชวน", "none", "studio_front.png", "none", "none"},
        {"", "สามที่หมาย", "none", "studio_front.png", "none", "none"},
        {"", "สามความรู้สึกที่ต่างกัน", "none", "studio_front.png", "none", "none"},
        {"", "คืนนี้… ผมต้องเลือกแล้ว", "none", "studio_front.png", "none", "none"},

        { "PLAYER", "เอ่อ ผมอยากไปเที่ยวกับ…", "none", "studio_front.png", "none", "",
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

        {"", "หลังเลิกงาน ไฟในสตูดิโอดับลงทีละดวง", "none", "studio.png", "none", "FADE_IN"},
        {"", "คนในกองเริ่มทยอยกลับ เหลือแค่เสียงเก็บอุปกรณ์เบา ๆ", "none", "studio.png", "none", "none"},

        {"", "ธีร์เดินเข้ามาใกล้พอดี", "Teeraphat.png", "hallway_office.png", "none", "FADE_IN"},
        {"TEER", "วันนี้ทำดี", "Teeraphat.png", "hallway_office.png", "none", "none"},
        {"PLAYER", "ขอบคุณครับ", "none", "hallway_office.png", "none", "none"},

        {"", "เขามองผมนิ่ง ๆ เหมือนจะพูดอะไร แต่ก็หยุดไว้", "Teeraphat.png", "hallway_office.png", "none", "none"},
        {"TEER", "ไปกินข้าวกับผมไหม", "Teeraphat.png", "hallway_office.png", "none", "none"},
        {"TEER", "ไม่ต้องคิดมาก… แค่อยากให้คุณได้พัก", "Teeraphat.png", "hallway_office.png", "none", "none"},

        {"", "รถจอดใต้ตึก เมืองกลางคืนสว่างนุ่ม ๆ จากไฟถนน", "none", "night_city.png", "none", "FADE_IN"},
        {"", "ธีร์ไม่ได้พูดเยอะ แต่เขาขับช้า… เหมือนกลัวผมเหนื่อย", "none", "night_city.png", "none", "none"},

        {"", "ร้านอาหารเงียบ ๆ เพลงเบา และแสงอุ่น", "none", "restaurant_night.png", "none", "FADE_IN"},
        {"", "เราคุยเรื่องงาน… เรื่องไร้สาระ… และเรื่องที่ผมคิดว่าผมจะไม่เล่าให้ใครฟังอีก", "none", "restaurant_night.png", "none", "none"},

        {"", "พอกลับมาส่งที่หน้าคอนโด ธีร์เดินมาหยุดตรงหน้าผม", "Teeraphat.png", "condo_front.png", "none", "FADE_IN"},
        {"", "สายตานิ่งเหมือนเดิม… แต่ไม่กดดันเหมือนเดิม", "Teeraphat.png", "condo_front.png", "none", "none"},

        {"TEER", "ผมไม่รีบ", "Teeraphat.png", "condo_front.png", "none", "none"},
        {"TEER", "แต่ผมอยากเป็นคนที่คุณเลือก… แบบตั้งใจ", "Teeraphat.png", "condo_front.png", "none", "none"},

        {"", "ลมหายใจผมหยุดไปหนึ่งจังหวะ", "none", "condo_front.png", "none", "none"},
        {"PLAYER", "…ถ้าอย่างนั้น", "none", "condo_front.png", "none", "none"},
        {"PLAYER", "เป็นแฟนผมไหมครับ", "none", "condo_front.png", "none", "none"},

        {"", "ธีร์เลิกคิ้วนิด ๆ เหมือนจะแกล้ง", "Teeraphat.png", "condo_front.png", "none", "none"},
        {"TEER", "คุณนี่…", "Teeraphat.png", "condo_front.png", "none", "none"},
        {"TEER", "ผมรอให้คุณพูดประโยคนี้ทั้งวันแล้ว", "Teeraphat.png", "condo_front.png", "none", "none"},

        {"", "เขายิ้มมุมปาก… แต่ครั้งนี้มันอุ่น", "Teeraphat.png", "condo_front.png", "none", "none"},
        {"TEER", "ครับ", "Teeraphat.png", "condo_front.png", "none", "none"},
        {"TEER", "เป็น", "Teeraphat.png", "condo_front.png", "none", "none"},

        {"", "โลกไม่ได้กลับมาสมบูรณ์ทันที", "none", "black.jpg", "none", "FADE_TO_DARK"},
        {"", "แต่ผมรู้แล้ว… ว่าผมไม่ต้องเดินคนเดียว", "none", "black.jpg", "none", "none"},
        {"", "— HAPPY END (ธีร์) —", "none", "black.jpg", "none", "FADE_OUT_TO_NEXT"}
    };


    // =========================
    // SCENE_TIAN_END: Happy End (เทียน)
    // =========================
    public static final Object[][] SCENE_TIAN_END = {

        {"", "หลังเลิกงาน เทียนเดินมาหยุดข้าง ๆ แบบไม่ให้ผมต้องรีบตอบอะไร", "Tian.png", "hallway_office.png", "none", "FADE_IN"},
        {"TIAN", "พี่คิดว่าจะซื้อเครื่องดื่มเลี้ยงทุกคนในกองถ่าย", "Tian.png", "hallway_office.png", "none", "none"},
        {"TIAN", "อยากจะไปกับพี่ไหม", "Tian.png", "hallway_office.png", "none", "none"},

        {"", "คำชวนธรรมดา… แต่ทำให้ผมรู้สึกเหมือนมีที่ให้ยืน", "none", "hallway_office.png", "none", "none"},
        {"PLAYER", "ไปครับ", "none", "hallway_office.png", "none", "none"},

        {"", "ร้านเครื่องดื่มเล็ก ๆ หน้าออฟฟิศ คนคุยกันเบา ๆ", "none", "drink_shop.png", "none", "FADE_IN"},
        {"", "เทียนจำได้ว่าผมชอบหวานน้อย ทั้งที่ผมยังไม่ทันพูด", "Tian.png", "drink_shop.png", "none", "none"},
        {"PLAYER", "พี่จำได้ด้วยเหรอครับ", "none", "drink_shop.png", "none", "none"},
        {"TIAN", "พี่จำได้หลายอย่าง", "Tian.png", "drink_shop.png", "none", "none"},
        {"TIAN", "โดยเฉพาะตอนที่คุณพยายามเข้มแข็งคนเดียว", "Tian.png", "drink_shop.png", "none", "none"},

        {"", "หลังแจกเครื่องดื่มเสร็จ เทียนพาผมเดินเลี่ยงไปทางระเบียงเงียบ ๆ", "none", "office_balcony.png", "none", "FADE_IN"},
        {"", "ลมเย็นพัดผ่าน เมืองเหมือนช้าลง", "none", "office_balcony.png", "none", "none"},

        {"TIAN", "พี่ไม่อยากเร่งคุณ", "Tian.png", "office_balcony.png", "none", "none"},
        {"TIAN", "แต่อยากให้คุณรู้ว่า… ถ้าวันไหนอยากพิงใคร", "Tian.png", "office_balcony.png", "none", "none"},
        {"TIAN", "พี่ยืนอยู่ตรงนี้ได้", "Tian.png", "office_balcony.png", "none", "none"},

        {"", "ผมหัวเราะเบา ๆ แบบคนที่เพิ่งได้หายใจ", "none", "office_balcony.png", "none", "none"},
        {"PLAYER", "งั้น…", "none", "office_balcony.png", "none", "none"},
        {"PLAYER", "ผมขอพิงยาว ๆ ได้ไหมครับ", "none", "office_balcony.png", "none", "none"},

        {"", "เทียนยิ้ม—อุ่นจนผมลืมว่าตัวเองเคยเจ็บแค่ไหน", "Tian.png", "office_balcony.png", "none", "none"},
        {"TIAN", "ได้สิ", "Tian.png", "office_balcony.png", "none", "none"},
        {"TIAN", "เป็นแฟนกันไหม", "Tian.png", "office_balcony.png", "none", "none"},

        {"", "ผมพยักหน้า โดยไม่ต้องคิดนาน", "none", "black.jpg", "none", "FADE_TO_DARK"},
        {"PLAYER", "ครับ… เป็น", "none", "black.jpg", "none", "none"},
        {"", "— HAPPY END (เทียน) —", "none", "black.jpg", "none", "FADE_OUT_TO_NEXT"}
    };


    // =========================
    // SCENE_KIRIN_END: Happy End (คีริน) — งานวัด + ขอคบบนชิงช้าสวรรค์
    // =========================
    public static final Object[][] SCENE_KIRIN_END = {

        {"", "เลิกงานปุ๊บ คีรินก็วิ่งมาหาเหมือนกลัวผมหายไป", "Kirin.png", "hallway_office.png", "none", "FADE_IN"},
        {"KIRIN", "พี่ครับ! วันนี้มีมหกรรมงานวัดนะ", "Kirin.png", "hallway_office.png", "none", "none"},
        {"KIRIN", "ไปด้วยกันได้ไหม", "Kirin.png", "hallway_office.png", "none", "none"},
        {"KIRIN", "ผมอยากให้พี่ได้หัวเราะบ้าง", "Kirin.png", "hallway_office.png", "none", "none"},

        {"", "ผมไม่ทันได้ตอบยาว เขาก็ยิ้มกว้างเหมือนมั่นใจว่าผมจะไป", "none", "hallway_office.png", "none", "none"},
        {"PLAYER", "ไปครับ", "none", "hallway_office.png", "none", "none"},

        {"", "งานวัดสว่างด้วยไฟหลากสี เสียงดนตรี เสียงหัวเราะ และกลิ่นของของกิน", "none", "temple_fair.png", "none", "FADE_IN"},
        {"", "คีรินพาผมเดินไปทุกซุ้ม เหมือนอยากแชร์โลกทั้งใบให้ผม", "Kirin.png", "temple_fair.png", "none", "none"},
        {"KIRIN", "พี่เอาอันนี้!", "Kirin.png", "temple_fair.png", "none", "none"},
        {"KIRIN", "แล้วก็อันนี้ด้วย!", "Kirin.png", "temple_fair.png", "none", "none"},

        {"", "ผมเผลอหัวเราะออกมาจริง ๆ", "none", "temple_fair.png", "none", "none"},
        {"", "คีรินหันมามองผมทันที เหมือนเห็นของมีค่าที่สุด", "Kirin.png", "temple_fair.png", "none", "none"},

        {"", "ชิงช้าสวรรค์หมุนช้า ๆ เหนือแสงไฟทั้งงานวัด", "none", "ferriswheel.png", "none", "FADE_IN"},
        {"KIRIN", "พี่ขึ้นกับผมนะ", "Kirin.png", "ferriswheel.png", "none", "none"},

        {"", "ในกระเช้าเล็ก ๆ เสียงข้างนอกค่อย ๆ หายไป เหลือแค่ลมหายใจ", "none", "ferriswheel_inside.png", "none", "FADE_IN"},
        {"", "คีรินนั่งนิ่งกว่าปกติ เหมือนเก็บความกล้าอยู่", "Kirin.png", "ferriswheel_inside.png", "none", "none"},

        {"KIRIN", "พี่ครับ…", "Kirin.png", "ferriswheel_inside.png", "none", "none"},
        {"KIRIN", "ผมรู้ว่าผมเด็กกว่า", "Kirin.png", "ferriswheel_inside.png", "none", "none"},
        {"KIRIN", "แต่ผมไม่ได้เล่น ๆ นะ", "Kirin.png", "ferriswheel_inside.png", "none", "none"},

        {"", "ชิงช้าสวรรค์ขึ้นถึงจุดสูงสุด พอดีกับจังหวะที่เขาสบตาผม", "Kirin.png", "ferriswheel_inside.png", "none", "none"},
        {"KIRIN", "เป็นแฟนผมได้ไหมครับ", "Kirin.png", "ferriswheel_inside.png", "none", "none"},

        {"", "ผมมองเขานานกว่าที่คิด", "none", "ferriswheel_inside.png", "none", "none"},
        {"", "แล้วค่อย ๆ ยิ้ม", "none", "ferriswheel_inside.png", "none", "none"},
        {"PLAYER", "ได้ครับ", "none", "ferriswheel_inside.png", "none", "none"},
        {"PLAYER", "แต่ห้ามทำให้ผมปวดหัวนะ", "none", "ferriswheel_inside.png", "none", "none"},

        {"", "คีรินทำหน้าดีใจจนเหมือนโลกสว่างขึ้นอีกระดับ", "Kirin.png", "ferriswheel_inside.png", "none", "none"},
        {"KIRIN", "สัญญาครับ!", "Kirin.png", "ferriswheel_inside.png", "none", "none"},
        {"", "— HAPPY END (คีริน) —", "none", "black.jpg", "none", "FADE_TO_DARK"}
    };


    // =========================
    // SCENE_TRUE_END: True Ending (1-1-1) — อยู่กับตัวเอง / เป็นเพื่อนกันได้
    // =========================
    public static final Object[][] SCENE_TRUE_END = {

        {"", "หนึ่งสัปดาห์ผ่านไป", "none", "bedroom.png", "BGM_Warm", "FADE_IN", "CAPTION"},
        {"", "ชีวิตกลับมาเดินช้า ๆ ในจังหวะที่พอดี", "none", "bedroom.png", "none", "none"},
        {"", "ผมยังไปทำงาน ยังยิ้ม ยังเหนื่อย", "none", "bedroom.png", "none", "none"},
        {"", "แต่หัวใจผมไม่วิ่งหนีตัวเองเหมือนเดิมแล้ว", "none", "bedroom.png", "none", "none"},

        {"", "คืนนั้น มือถือสั่นอีกครั้ง", "none", "bedroom.png", "SFX_Notify", "none"},
        {"", "แล้วก็อีกครั้ง", "none", "bedroom.png", "SFX_Notify", "none"},
        {"", "แล้วก็อีกครั้ง", "none", "bedroom.png", "SFX_Notify", "none"},

        {"", "ผมกดเข้าไป", "none", "dating_chatlist.png", "none", "FADE_IN"},

        {"TEER", "เราควรคุยกันเรื่อง ‘สถานะ’ ไหม", "Teeraphat.png", "dating_chat.png", "none", "none"},
        {"TIAN", "พี่ไม่อยากกดดัน แต่พี่อยากรู้ว่าคุณโอเคแค่ไหน", "Tian.png", "dating_chat.png", "none", "none"},
        {"KIRIN", "พี่ครับ… ผมจริงจังนะ", "Kirin.png", "dating_chat.png", "none", "none"},

        {"", "ผมมองชื่อทั้งสามคนอยู่นาน", "none", "bedroom.png", "none", "FADE_IN"},
        {"", "แล้วก็เข้าใจอะไรบางอย่าง", "none", "bedroom.png", "none", "none"},
        {"", "ผมไม่ได้ไม่ชอบใคร", "none", "bedroom.png", "none", "none"},
        {"", "ผมแค่… ยังไม่พร้อมให้ใครต้องเจ็บเพราะความลังเลของผม", "none", "bedroom.png", "none", "none"},

        {"", "ผมพิมพ์ข้อความยาวที่สุดในรอบหลายเดือน", "none", "dating_chat.png", "none", "FADE_IN"},
        {"PLAYER", "ขอบคุณที่เข้ามาในชีวิตผมนะครับ", "none", "dating_chat.png", "none", "none"},
        {"PLAYER", "แต่ตอนนี้… ผมอยากค่อย ๆ อยู่กับตัวเองก่อน", "none", "dating_chat.png", "none", "none"},
        {"PLAYER", "ผมยังอยากมีพวกคุณในชีวิต", "none", "dating_chat.png", "none", "none"},
        {"PLAYER", "ในฐานะเพื่อน… ได้ไหมครับ", "none", "dating_chat.png", "none", "none"},

        {"", "ความเงียบเกิดขึ้นไม่กี่วินาที", "none", "bedroom.png", "none", "FADE_IN"},
        {"", "แล้วข้อความตอบกลับก็ขึ้นทีละคน", "none", "bedroom.png", "none", "none"},

        {"TEER", "เข้าใจ", "Teeraphat.png", "dating_chat.png", "none", "none"},
        {"TEER", "ดูแลตัวเองให้ดี", "Teeraphat.png", "dating_chat.png", "none", "none"},

        {"TIAN", "ได้สิ", "Tian.png", "dating_chat.png", "none", "none"},
        {"TIAN", "พี่อยู่ตรงนี้เสมอ", "Tian.png", "dating_chat.png", "none", "none"},

        {"KIRIN", "งั้นผมจะเป็นเพื่อนที่พี่ไว้ใจได้ที่สุดเลย!", "Kirin.png", "dating_chat.png", "none", "none"},

        {"", "ผมวางมือถือ แล้วหายใจลึก ๆ", "none", "bedroom.png", "none", "none"},
        {"", "ครั้งแรกที่ความเงียบ… ไม่ได้น่ากลัว", "none", "bedroom.png", "none", "none"},
        {"", "เพราะผมไม่ได้ถูกทิ้ง", "none", "bedroom.png", "none", "none"},
        {"", "ผมแค่เลือกตัวเอง", "none", "bedroom.png", "none", "none"},

        {"", "— TRUE END (อยู่กับตัวเอง) —", "none", "black.jpg", "none", "FADE_TO_DARK"}
    };
}