package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.Iterator;

public class DamageTextManager implements Disposable {
    private final Array<DamageText> damageTexts;
    private final BitmapFont font;
    private final FreeTypeFontGenerator fontGenerator;

    public DamageTextManager() {
        damageTexts = new Array<>();

        // 폰트 로드 (font 폴더)
        FreeTypeFontGenerator tempGen = null;
        try {
            tempGen = new FreeTypeFontGenerator(Gdx.files.internal("font/Galmuri11.ttf"));
        } catch (Exception e) {
            tempGen = new FreeTypeFontGenerator(Gdx.files.internal("Galmuri11.ttf"));
        }
        fontGenerator = tempGen;

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;

        // ★★★ [수정] 한글 전체 포함 (데미지, 회피 등 텍스트용) ★★★
        String koreanCharacters = FreeTypeFontGenerator.DEFAULT_CHARS +
            "가각간갇갈감갑값강갖같갚낳내너네노누느니다단달담답당더던덜덤덥덩도독돈돌동돼되된두드든들듬듭등디따때떠떨떻떼또뚜뚫뛰뜨뜻띠라락란랄람랍랑래러럭런럴럼럽렁레려력련렬렴렵령례로록론롤롬롭롱뢰루룩룬룰룸룹룽류륙륜률륨륭르흑흔흘흠흡흥희히" +
            "거걱건걷걸검겁것겉게겨격겪견결겸겹경곁계고곡곤곧골곰곱곳공과곽관괄광괘괜괴굉교구국군굳굴굵굶굽궁권귀규균그극근글긁금급긋긍기긴길김깁깃깊까깎깐깔깜깝깡깨꺼꺾껀껄껌껍껑께껴꼬꼭꼰꼴꼼꼽꽁꽂꽃꽈꽉꾀꾸꾹꾼꿀꿇꿈꿉꿍끄끈끊끌끎끓끔낍깅나낙낚난날낡남납낫낭낮낯낱" +
            "냐향허헌헐험헛헤헬혀현혈협형혜호혹혼홀홈홉홍화확환활황회획횟횡효후훈훌훔훨휘휴흉흐흑흔흘흙흠흡흥흩희흰히힘" +
            "아악안앉않알앓암압앗앙앞애액야약얀얄얇얌양얕얗얘어억언얹얻얼얽엄업없엇엉엌엎에엔엘여역엮연열엷염엽엿영예오옥온올옮옳옷와완왕왜왠외왼요욕용우욱운울움웃웅워원월웨웬위윗유육율으윽은을읊음읍응의이익인일읽잃임입잇있잊잎자작잔잖잘잠잡잣장잦재쟁저적전절젊점접젓정젖제젠져조족존졸좀좁종좋좌죄주죽준줄줌줍중쥐즈즉즐즘즙증지직진질짐집짓징짙짚짜짝짧째쨌쩌쩍쩐쩔쩜쩝쩡쪼쪽쫓쭈쭉찌찍찢차착찬찮찰참창찾채책챔챙처척천철첩첫청체쳐초촉촌총촬최추축춘출춤춥충취츠측층치칙친칠침칭카칸칼캄캐캔캠커컨컬컴컵컷케켓켜코콘콜콤콩쾌쿠쿡쿤쿨쿵퀄퀴크큰클큼키킬타탁탄탈탑탓탕태택탬터턱털텅테텍텔템토톤톨톱통퇴투퉁튀튜트특튼틀틈티틱팀팅파팎판팔패팩팬퍼퍽페펜펴편펼평폐포폭표푸푹풀품풍퓨프플픔피픽필핏핑하학한할함합항해핵햄햇행" +
            "CritMssBlockdmg0123456789"; // 영문/숫자 추가

        parameter.characters = koreanCharacters;

        font = fontGenerator.generateFont(parameter);
        font.setColor(1, 0, 0, 1);
    }

    public void createDamageText(int damage, float x, float y) {
        damageTexts.add(new DamageText(String.valueOf(damage), x, y));
    }

    public void update(float deltaTime) {
        for (Iterator<DamageText> iter = damageTexts.iterator(); iter.hasNext(); ) {
            DamageText dt = iter.next();
            dt.update(deltaTime);
            if (dt.isFinished()) {
                iter.remove();
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (DamageText dt : damageTexts) {
            dt.draw(batch, font);
        }
    }

    @Override
    public void dispose() {
        font.dispose();
        fontGenerator.dispose();
    }
}
