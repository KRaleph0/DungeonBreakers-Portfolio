package com.team.dungeonbreakers;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashSet;
import java.util.Set;

public class DungeonBreakersGame extends Game {
    public static final float V_WIDTH = 1600;
    public static final float V_HEIGHT = 900;
    // ★★★ [수정] PPM을 원래 값 32.0f로 복구 ★★★
    public static final float PPM = 32.0f;

    public static final short GROUND_BIT = 1;
    public static final short PLAYER_BIT = 2;
    public static final short ENEMY_BIT = 4;
    public static final short DESTROYED_BIT = 8;
    public static final short OBJECT_BIT = 16;
    public static final short ENEMY_HEAD_BIT = 32;
    public static final short ITEM_BIT = 64;
    public static final short PROJECTILE_BIT = 128;
    public static final short TRIGGER_BIT = 256;
    public static final short COIN_BIT = 512;
    public static final short PLATFORM_BIT = 1024;
    public static final short ENEMY_PROJECTILE_BIT = 2048;
    public static final short NPC_BIT = 4096;
    public static final short PLAYER_WEAPON_BIT = 8192;

    public SpriteBatch batch;
    public BitmapFont uiFont;
    public BitmapFont menuFont;

    public Texture cursorTexture;
    public Texture normalCursorTexture;

    public CharacterManager characterManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        characterManager = new CharacterManager();
        characterManager.loadAssets();
        initFonts();
        initCursors();

        setScreen(new MainMenuScreen(this));
    }

    private void initFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/Galmuri11.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.size = 24;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + getAllKoreanCharacters();
            uiFont = generator.generateFont(parameter);

            parameter.size = 48;
            menuFont = generator.generateFont(parameter);

            generator.dispose();
        } catch (Exception e) {
            uiFont = new BitmapFont();
            menuFont = new BitmapFont();
        }
    }

    private String getAllKoreanCharacters() {
        StringBuilder sb = new StringBuilder();
        // (기존 한글 목록 유지)
        String common = "가각간갇갈감갑값갓갔강갖같갚개객거걱건걷걸검겁것게겨격견결겸경계고곡곤곧골곰곱곳공과곽관광괘괴교구국군굳굴굵굶굽굿궁권귀규균그극근글긁금급긋기긴길김깊까깍깎깐깔깜깝깨꺼꺾껍께껴꼬꼭꼴꼼꼽꽂꽃꽉꽤꾸꿀꿈꿔끄끈끊끌끓끔끝끼끼나낙낚난날낡남납낫낭낮낯낱낳내냄냇냉냐너넉넌널넓넘넣네넥넷녀년념녕노녹논놀놈농높놓뇌누눈눌눕뉘뉴느늑늘늙능늦니닉닌닐님다닥닦단닫달닭닮담답닷당닿대댁댄댐댓더덕던덜덤덥덧덩덮데델도독돈돌돕동돼되된될됨두둔둘둠둡둥뒤드득든듣들듬듭등디딩따딱딴딸땀땅때떠떡떤떨떻떼또똑뚜뚫뚱뛰뜨뜯뜰뜻띄라락란랄람랍랑래랙랜램랩랫랭랴러럭런럴럼럽럿렁렇레렉렌렐렘렙렛렝려력련렬렴렵령례로록론롤롬롭롯롱뢰루룩룬룰룸룹룻룽뤄뤼뤽륀륄류륙륜률륭르륵른를름릅릇릉릎리릭린릴림립릿링마막만많맏말맑맘맙맛망맞맡맣매맥맨맴맵맺머먹먼멀멈멋멍메멘멜멤멥멧며멱면멸명모목몫몬몰몸몹못몽무묵묶문묻물뭄뭇뭐뭘미민믿밀밉밌밋바박밖반받발밝밟밤밥방밭배백뱀뱃뱉버번벌범법벗베벤벨벼벽변별병볕보복볶본볼봄봅봉봐봤뵈뵙부북분불붉붐붓붕붙뷰브블비빌빔빗빚빛빠빨빵빼뺨뻐뻔뻗뼈뽑뿌뿐쁘쁨삐사삭산살삶삼삽삿상새색샌샘샛서석섞선설섬섭섯성세센셀셈셉셋셔션소속손솔솜솝솟송솥쇄쇠쇼수숙순술숨숫숭숯숲쉬쉰쉽스슥슨슬슴습슷승시식신실싫심십싱싸싹쌀쌍쌓써썩썰썹쏘쏟쑤쓰쓸씀씌씨씩씬씹씻아악안앉않알앓암압앗앙앞애액야약양어억언얹얻얼엄업없엇엉에엔엘여역연열염엽엿영옆예오옥온올옮옳옷와완왕왜외왼요욕용우욱운울움웃웅워원월웨위윗유육윤율으윽은을읊음읍응의이익인일읽잃임입잇있잊잎자작잔잖잘잠잡잣장잦재쟁저적전절젊점접젓정젖제져조족존졸좀좁종좋좌죄주죽준줄줌줍중쥐즈즉즐즘증지직진질짐집짓징짜짝짠짧짱째쩌쩍쩐쩔쩜쪽쫓쭈쭉찌찍찢차착찬찮찰참창찾채책챙처척천철첨첩첫청체쳐초촉촌총촬최추축춘출춤춥충취츠측츰층치칙친칠침칭카칸칼캄캐캠커컨컬컴컵컷켓켜코콘콜콤콥콩쾌쿠쿡쿤쿨쿰쿵쿼퀘퀴큐크큰클큼키킥킬킹타탁탄탈탑탓탕태택탠탤탬탭탱터턱턴털텀텁텃텅테텍텐텔템텝텡토톡톤톨톰톱통퇴투퉁튀튜트특튼틀틈티틱틴틸팀팁팅파팍판팔패팩팬팰팸팹팽퍼퍽펀펄펌펑페펙펜펠펨펩펭펴편펼평폐포폭폰폴폼표푸푹푼풀품풍퓨프플픔피픽필핍핑학한할함합항해핵핸햄햇행향허헌헐험헤헬혀현혈협형혜호혹혼홀홈홉홍화확환활황회획횟효후훈훌훔훨휘휴흉흐흑흔흘흙흡흥흩희흰히힘대쉬쿨타임";
        sb.append("상점구매보유골드레벨단계보스휴식상자전투엘리트대쉬쿨타임직업잠금김해금됨높은등급등장칸슬롯");
        collectItemDataCharacters(sb);
        return sb.toString();
    }

    private void collectItemDataCharacters(StringBuilder sb) {
        // (JSON 로드 로직 생략)
        String[] files = {
            "item/armors.json", "item/boots.json", "item/helmets.json",
            "item/knignt_weapon.json", "item/Archer_weapon.json",
            "item/shields.json", "item/quivers.json", "item/consumables_all.json"
        };
        JsonReader jsonReader = new JsonReader();
        for (String fileName : files) {
            try {
                FileHandle handle = Gdx.files.internal(fileName);
                if (handle.exists()) {
                    JsonValue root = jsonReader.parse(handle);
                    JsonValue items = root.get("items");
                    for (JsonValue item : items) {
                        sb.append(item.getString("name", ""));
                        sb.append(item.getString("description", ""));
                    }
                }
            } catch (Exception e) {}
        }
    }

    private void initCursors() {
        try {
            cursorTexture = new Texture(Gdx.files.internal("img/UI/Cursor.png"));
            normalCursorTexture = new Texture(Gdx.files.internal("img/UI/cursor_Normal.png"));
        } catch (Exception e) { }
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (uiFont != null) uiFont.dispose();
        if (menuFont != null) menuFont.dispose();
        if (cursorTexture != null) cursorTexture.dispose();
        if (normalCursorTexture != null) normalCursorTexture.dispose();
        if (characterManager != null) characterManager.dispose();
    }
}
