#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 7 (input lines 601-700). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409676: """sapfear: your specialists uploaded the news very creatively
vern: for example? :)
vern: oh shit
vern: I mean, oh, how creative""",
409677: """A comment on the demotivator "Elf girls - a nerd's pipe dream".
ххх: Why pipe dream? Those ears cost 50 rubles""",
409678: """Философский Саксаул:
Judging by the photo, you're a Russian person. Then where does "Рисую созданий" come from? What language is that? Or is "useless and ghostly buildings" fine as is?

Кот:
Judging by your profile photo, you're a billiard table. Then where does such talkativeness, uncharacteristic of the given object, come from? ;)""",
409679: """xxx: A thesis on Chuck Norris is so cool it doesn't need defending.""",
409680: """wer: damn, there's a kid running around our office, like 12 years old. I thought he was some employee's son. Turned out to be Irina, over 20, a courier.""",
409681: """xxx: Looked everywhere for a lost earbud tip.
xxx: Looked in the hallway, corridor, under the bed.
xxx: Turned out to be in my ear.""",
409682: """Nightchild: Selling used D. Dontsova books.
A big collection of books in both hard and soft cover.
Root: got any in rolls?""",
409683: """poligraf: I've got an acquaintance family, husband and wife - lawyers. The husband's dad - a judge. The wife's parents are lawyers too.
poligraf: God forbid a divorce.""",
409684: """From a photo hobbyists forum

Чекан: the landscape is excellently chosen, bright, saturated colors, but you need to wipe the dust off the lens!
Let_Jet: wipe your glasses)
Чекан: thanks!""",
409685: """[БЖ] - Pregnant wife, [М] - her husband.
БЖ: - You're not hoping to be present at the birth, are you?
М: - No, I don't want to.
БЖ: - Me neither...""",
409686: """Пироман: today the shawarma turned me into the Hulk - I turned green and very angry.""",
409687: """Игра: Today I was happily making myself a hair mask (kefir, cocoa, henna), coming back from the bathroom, and my husband is sitting in the kitchen eating up my mask!!! (""",
409688: """<xxx> Every time our six-foot-six art director tells someone "Щас I'll give you the psd", the office involuntarily freezes.""",
409689: """xxx: Yesterday turned on the ending of Terminator 3 on TV.
xxx: Thought it was the news... Thought, well, it started...""",
409690: """xxx: and yesterday I bought nails)
xxx: I came and said: give me nails. seller: what kind? me: the kind that go into the wall. seller (slightly stunned): at least what length? me: well.. not very long.. so they don't go through the neighbor's wall. seller(stunned): how many? me: 4. seller: 4 kg? me: what, they're measured in KILOGRAMS?! I need 4 nails!
xxx: the seller looked at me sadly and gave me 4 nails for free)""",
409691: """лИсЕнА: I hate you, you bastard!!!! What kind of man are you, fuck!!!! You can't even put a proper period in a relationship!!!!
Skjoldy: .""",
409692: """romashka: the cats pulled my jacket out of the closet (!!!) put it on the floor, and sleep on it
romashka: sometimes it seems like I'm their pet""",
409693: """xxx: give me your sister's number! urgently!
yyy: why do you need it?
xxx: she's a medic, and I need a medical consultation
yyy: she's a pathologist. are you sure?""",
409694: """zhenya: reading a salesman's resume
zhenya: Language skills: with a dictionary
zhenya: Computer skills: borderline user""",
409695: """XXX: The game "believe it or not" is played by children from 3 to 12 years old...
YYY: Yeah, and the game "believe it or not this self-signed SSL certificate" is played by overgrown bearded children aged 20 to 50...""",
409696: """xxx: sorry please, had to step away
xxx: a total fuckup happened at work
yyy: did you at least resolve your fuckup?
xxx: you kidding, it's a fuckup not a trifle, takes a couple days""",
409697: """Neko-Punk: Yesterday the duty officer told me the sysadmin is furious because of what I did with the website yesterday. I was surprised. Today I talked to a comrade (he works here too), the comrade was also surprised, said he had no idea. But the most surprised was the sysadmin when he learned from the cashier that he'd been furious at me =D""",
409698: """Izza: A colleague is moving to a new apartment. On one side - the city hospital morgue. On the other - a cemetery nearby
Izza: a residential neighborhood, basically""",
409699: """Discussion of some religious content website.

xxx: I was touched by "Subscribe for when the messiah comes". One hell of an email message - "We notify you that the third coming happened today. Have a nice day".""",
409700: """xxx: sick?
xxx: steam your feet, eat pills, breathe over potatoes under a blanket!
xxx: by the way, if you take a fork under the blanket too, you can sit there for a really long time.""",
409701: """pups: Be simpler!
ameba: How much simpler...""",
409702: """xxx: Ever since I learned that cottage cheese isn't mined from vareniki, I've been suspicious of any food.""",
409703: """xxx: girls, how much does the simplest-tiniest sewing machine cost?
yyy: the smallest sewing machine is a needle""",
409704: """xxx: I wonder, if a penguin falls, can it get up on its own?
yyy: are you talking about linux?""",
409705: """LeMurzik: Walking down the street right now like a peahen - oncoming men are falling, guys look into my eyes and fall, one grandpa even took a tumble... And I've got spikes built into my sneakers, I don't give a damn about the ice -)""",
409706: """xxx: what are you doing tonight?
yyy: going to the pool. don't you want to come with us?
xxx: we were gonna sit somewhere...
yyy: well sit in the water""",
409707: """xxx: A friend got into the regional track and field team.
xxx: Just waiting for him to become famous and be asked "why did you go into this sport?"
xxx: Because the decision was made after watching porn with a gymnast!""",
409708: """@IRAkezzz: On Saturday decided to put the cat on a diet, she'd gotten too fat. And today she gave birth. What are YOU willing to do for food?!""",
409709: """Ванильная королева: How much time and effort left on all sorts of fools... I don't need cables anymore... I don't miss them..
Dolbaeb: went wireless or something?""",
409710: """xxx: my building used to say "Nirvana", "Киш" and "dick"..
xxx: and now it says "Firefox", and "+1\"""",
409711: """Standing in a pink pajama in the kitchen, cooking breakfast. Husband sits behind me on a stool sharpening a knife: skyr-skyr, skyr-skyr... And suddenly says:
- Honey, you're so defenseless in this pajama!
And on with the knife on the sharpener: skyr-skyr, skyr-skyr...""",
409712: """Job site, classifieds board:
ххх: Serious work, for a serious person, with serious pay.
ууу: Who needs killing?""",
409713: """lolmode: I'll teach you to scam people out of money, 50% payment before training, 50% after. Knock in PM.""",
409714: """xxx: They want to have a new kid
yyy: And what happened to the old one?""",
409715: """xxx: A new social network for mathematicians - "In the Compact" :)""",
409716: """xxx: tell me a bedtime story
yyy: once upon a time Masha was walking through the forest
yyy: and sees three cars burning
yyy: got into the first one, sat - no, too big. got into the second one
xxx: ... "Who burned up in MY car?!" )""",
409717: """habr comments on the first Russian communicator with a touch screen

xxx:
well, the first pancake's a lump

yyy:
AvtoVAZ's experience proves that the number of "lumpy pancakes" is limited only by the total number of pancakes.

zzz:
You're wrong, AvtoVAZ figured it all out long ago, stopped baking pancakes and started baking little lumps""",
409718: """ххх: You can get a pool certificate in the office from the corporate doctor. Or from the designers.""",
409719: """<DAJ> my buddy once asked his dad for the car to "drive around"
<DAJ> his father made him drive through three cities in GTA San Andreas without getting into an accident""",
409720: """<[sky]snob> going to chomp
<[Sky]Куплю_БК0011> To crap and spin the wheel?""",
409721: """From a forum:
Today observed. Went to visit a buddy who works at the central city boiler plant. And they're getting ready to start the boiler, and just then engaged the gas analyzer to determine the quality of the natural gas used for firing the boiler.
Gas analyzer device:
1. Body - galvanized bucket.
2. Working medium - soap solution.
3. Gas supply device - hose with a metal tip and a tap, connected from the pipe going to the boiler nozzles.
Gas analysis method - feed some natural gas into the bucket with soap solution until a huge soap bubble forms. Then throw a burning match into the bubble. If it bangs well - means good, quality gas. If it bangs poorly or doesn't bang at all - means gas must not be fed to the nozzles, and you need to call the management and the gas distribution station and sort it out.
The method gives practically a hundred-percent result.
I wonder what devices are used for the same purpose in other countries, and how much they cost?""",
409722: """falc: yesterday walking home from work, a woman in front of me slipped and fell. Turned on the knight, walk up, extend my hand - and fall down right next to her. Broke my arm, then she drove me to the ER :D""",
409723: """ХХХ: Nanotech rules! Three days ago I managed to unclog a sewer with broken fiber optic""",
409724: """xxx: for some reason it always stresses me out when in organizations providing printing services they give me change in new uncrumpled banknotes""",
409725: """препод: while I'm reading you lectures ...
я:(to a friend in a whisper) ... the enemy is swinging!!!""",
409726: """ххх: African dust, captured by a storm over the northern regions of Egypt, on Thursday-Friday may fall as "colored" snow in the south of Western Siberia, in the areas of Barnaul, Omsk, Novosibirsk or Tomsk.

yyy: Get out your swimsuits and flippers. An Egyptian resort is flying our way!!!""",
409727: """xxx: They run ads on the radio - "Pay your utility bills at Russian Post! It's fast and convenient!"
xxx: And I thought knowingly false information in advertising was banned by law...""",
409728: """xxx: come on mom, you know I can't just walk past an idiot
xxx: I either need to argue, or befriend, or marry him""",
409729: """[Вятка]: I'll show you who's a washing machine and who's a beautiful old Russian city""",
409730: """Shaksy Green: I trim her claws regularly too, but familiar cat people said that from this the paws of the kitty get "unbalanced", and she'll become more clumsy (it'll be harder to jump anywhere, she'll "miss" more often).
LEI: After the claw trimming please proceed to cat-service for "wheel alignment\"""",
409731: """xxx: walking to work, look - drops of blood on the road
xxx: a real trail of drops goes
xxx: thinking - where will it lead?
xxx: walked along the drops
xxx: and saw a man dragging a sack of beets""",
409732: """стрелок: "kids are wonderful: wherever you sit down - there's a cube in your ass". (c) one acquaintance of mine.""",
409733: """xxx: You don't know how a circle differs from a circumference?
yyy: Welll, a circle can be a life buoy.""",
409734: """Puzzle: working at the tax office taught me one thing - don't poke into shit you don't know, no matter how cool it looks from the outside""",
409735: """9th: I've been lucky with developers lately
9th: just how do you think to make it so that to press a button you have to hover the mouse over it and press space""",
409736: """сабж - shashlik

111: Confirm. At our company there was an Armenian guy, he said you don't need to soak it in anything at all, salt - right before cooking, of spices - only pepper (a little), and best to fry on coals from grapevine twigs.

222: not from grapevine twigs, but from grapevine seeds! there was even a song about a grapevine seed!

333: Coals from grapevine seeds - an exquisite thing. They fry nightingale tongue patties on them.""",
409737: """SergioS: before, when I sat down to eat, I'd turn on another episode of my favorite series - lunch took exactly one episode. All would be fine, but now I start salivating when TV shows a rerun of this series...:)""",
409738: """(н)-boss, (я)-me
Я: When will they raise the salary?
Н: This month they wanted to lower it, since there's no money. Did they lower it?
Я: No
Н: Consider it raised""",
409739: """Talia: Finally washed the permanently splattered mirror in the bathroom - now I look at myself in excellent quality: if before it was 240 at best, now it's a full 720.""",
409740: """Марина: today read that lard should be eaten without bread
Марина: during a diet""",
409741: """xxx: A drummer is like a healer in a party
xxx: Nobody notices his work until he screws up""",
409742: """xxx: My dad is harsh - when advertising bots add him, he sends them his own ads""",
409743: """xxx: Today in the store they gave me change with a torn ten.
yyy: So why didn't you return it and ask for a replacement.
xxx: I was scared to argue with that saleswoman, 'cause the ten was metal...""",
409744: """xxx: If from Sunday to Monday you dream that the alarm clock is ringing - the dream is prophetic )""",
409745: """n2ck: well tell me
Kumagoro: you guess!
n2ck: no, tell me! I'm not allowed to guess!
Kumagoro: why?
n2ck: I have a childhood psychological trauma: played "Guessing" and broke my leg""",
409746: """printf: How cool it would be - to hire a person to sleep instead of you, free up another +8 hours for working on your own projects...
dem0n3d: Hire me! Cheap!""",
409747: """xxx: The weather gadget just warned that it's -26° outside and raining. Probably from sulfur""",
409748: """Кириатур: Tomorrow I'll dye it black
Сэйф: Goth?
Кириатур: You're the goth
Кириатур: I'm for the Romans""",
409749: """xxx: A comment like "Stood up for seeding" sounds kind of ambiguous on a porn tracker""",
409750: """xxx: The fundamental theorem of theoretical mechanics states that whoever doesn't know the Lagrange equation gets a fail on the exam""",
409751: """teh_6o6p:
Testament: after my death please place my mummified body in a display case on the square with a plaque : "How much longer, you bitches, do I have to wait?!\"""",
409752: """The Ministry of Education and Science allocated two million rubles for research that will help understand how to properly motivate young scientists and university teachers without involving material means.""",
409753: """Я: I remember my first computer... we were still small, dad brought some box. We ask: dad, dad, what's this? - it's a computer. Well, imagination ran wild, we'd already seen comps back then, though everything still ran under DOS, but there were games on them, some simple ones about a skier.
Я: when, oh when will dad assemble it! Hurry, I want to play so much. He assembled it.
Я: Turned out to be a vegetable box. Still standing on my balcony. Basically, my first comp disappointed me )""",
409754: """YYY: Roma fed the aquarium fish bloodworms, part of it managed to burrow into the substrate where the fish couldn't get it.
YYY: Now he's got mosquitoes in his room.""",
409755: """xxx: well we're gonna drill the basics there now, then it'll get harder
yyy: Oh! Oh, yes! God! Drill me! - she whispered passionately, holding out a black leather whip.
xxx: what's wrong with you?""",
409756: """about animals in slow motion:

b0b0: Cool thing! Birds and predators are interesting to watch in slow motion, but all kinds of reptiles, especially the turtle in episode 1...

evilflash: watching a turtle in slow motion? That's for masochists only.""",
409757: """ххх: Since 1861 the Russian people have been downloading their rights. Downloaded 2% so far.
ууу: It's just that Alexander II left the seeding in 1881. Need to re-upload.
ххх: and from 1917 to 1991 the whole torrent was shut down )
zzz: and now downloading is only for registered users.
zzz: and you can only register by invitation...""",
409758: """xxx: heart problems, as a rule, give complications to the liver""",
409759: """Candy: if I'm mad at you, that doesn't mean you can go whole days without writing to me""",
409760: """ххх: yesterday a computer store opened near us
ххх: "СчѢтоводъ - магазiнъ машiнъ дюже умныхъ"
ххх: went in to see what they've got
ххх: the era of the sign quite matches the era of the goods sold""",
409761: """<Capslock00> What dreams these days. Dreamed that in the future you can sell your brain, like a kidney.
<Lisa-Alisa> and what, did you sell it?
<Capslock00> Look at my nick.
<Capslock00> Of course I sold it )""",
409762: """xxx: argh! Tell me: why do non-biologists have to study biology, huh? What the hell for?!
yyy: errr... to understand us and the world around a bit more than at user level?""",
409763: """Dr.Ash: I think the cats are actually happy I got sick, think about it: lying with a temperature near 39, hot, barely moving, these demons have plastered themselves all over me and are purring)""",
409764: """а: Did you congratulate Irka on her daughter?
В: yeah, only now she's not talking to me
А: :) what for?
В: probably out of solidarity with her husband
А: and what's with him?
В: I suspect I shouldn't have, while congratulating her husband, asked whether he knows who the father is""",
409765: """Three ladies are gossiping at work, who have long and unsuccessfully been trying to get married. Well, either the men are too small, or without money, or some other mismatches.
So one of them tells:
- Here, an acquaintance is selling an apartment (further description, the apartment is good, the man is a director, with money, etc.). And also his wife left him, for some Italian (with even MORE money).
The other:
- Well then marry him!
The first:
- Why do I need him?? He's abandoned! Why do I need an abandoned one?

And again three ladies are gossiping, trying to get married...""",
409766: """Dre: Today I saw a real quaker's wife - went on a call to a kid with a fever. The mom opened the door, in the big room sits a guy, playing quakelive, in the small one - the sick child. Treated, already leaving, suddenly five times in a row QUAD DAMAGE!!! Mom:
"Damn, Vova, can't you share the BLUE RING again?! Go sit with your son!\"""",
409767: """Moms announced a recall of children of 1998 and 1999 model years from the streets, because dinner is getting cold.""",
409768: """alek_j: let me beat up your boyfriend and steal you
alek_j: only I have neither a horse nor a car, so I'll take you away in a grocery cart""",
409769: """Sam: was driving this morning, saw an ad for motor oil
Sam: "boosts the car's potency"
Sam: immediately thought "you'll be f**king your car even longer\"""",
409770: """at the seminar:
- And where's M***ov? Where's this boy, bursting out of his tight little pants in hope of procreation? Aa, with B***vy! What've you got going on there? Brokeback Mountain?""",
409771: """xxx: A client just called, said: "A Trojan horse and 7 suspicious substances were found on my computer"
yyy: The horse did its best...
yyy: tell him to move the mouse more carefully, or he'll smear it and get dirty!""",
409772: """Watching Hitchcock's "Rear Window"
xxx: imagine, before the internet appeared the main entertainment was watching the neighbors
xxx: window addiction
yyy: yeah, and parents going to work took the window's cord with them""",
409774: """XXX: Hi, need your help.
YYY: Listening.
XXX: Decided to take up sports. Ride around in our forest park in the evening. So I bought skis, poles with boots, a suit, and where do I get a rifle for biathlon now?""",
409775: """I walk into the fitness hall. There's just a suffocating smell of incense.
Я: Why does it smell so much like incense here?
Тренер: Dunno. They wrestled here this morning.
Я: With evil?""",
409776: """Silent-Mik: Hooray! The Opera of Aida finished downloading!
Innes: I have Google Chrome.""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 601, 700  # 1-based line numbers
    lines = open(src, encoding="utf-8").read().splitlines()
    assert len(lines) >= end, f"only {len(lines)} lines"
    # Build and validate everything BEFORE appending (avoids partial writes).
    out = []
    want_ids = []
    for ln in range(start, end + 1):
        r = json.loads(lines[ln - 1])
        en = EN.get(r["id"])
        assert en is not None, f"missing translation for id {r['id']} (line {ln})"
        assert en, f"empty en for id {r['id']}"
        assert en.count("\n") == r["text"].count("\n"), (
            f"newline mismatch id {r['id']}: en={en.count(chr(10))} ru={r['text'].count(chr(10))}")
        out.append(json.dumps({"id": r["id"], "ru": r["text"], "en": en}, ensure_ascii=False))
        want_ids.append(r["id"])
    # Resume safety: skip ids already present in dst.
    have = set()
    try:
        with open(dst, encoding="utf-8") as f:
            for l in f:
                l = l.strip()
                if l:
                    have.add(json.loads(l)["id"])
    except FileNotFoundError:
        pass
    new = [o for o, i in zip(out, want_ids) if i not in have]
    with open(dst, "a", encoding="utf-8") as f:
        for o in new:
            f.write(o + "\n")
    print(f"batch lines {start}-{end}: {len(new)} appended "
          f"({len(out) - len(new)} already present)")
