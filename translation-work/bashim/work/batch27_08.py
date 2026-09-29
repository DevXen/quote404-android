#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 8 (input lines 701-800). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409777: """- I got drunk as a dog
- if you got drunk like a dog today, then I'm like a cat
- what do you mean?
- got licked)))""",
409778: """Дизайнер: what friendship between pilots?
Клиент: what, there's no friendship between pilots?
Дизайнер: and how do you want me to depict that?
Клиент: dunno) but in my opinion it's some one symbol, a talisman or something like that""",
409779: """Rio: Yesterday, standing next to the road, I realized that the "nine" has very good brakes. It braked beautifully against a Kamaz.""",
409780: """CherryJB: I always thought in school
CherryJB: 5x5=25, 6x6=36
CherryJB: so why couldn't they make 7x7 be 47!?""",
409781: """Topic on a forum about selling a piano.
Comment (spelling preserved):

By the way, I also sell a pianina, that is, giving it away for free. I keep it a floor above myself - over my bedroom. I'll send the address if a real buyer. the door just broke there - doesn't open, but if you come during the day, I'll help open it, help carry it out too, I'll even pay you money.

Also discounts if you take the musician with you.""",
409782: """xxx: just thought about what would happen if they create a real AI and it goes on lurk...""",
409783: """sys: Read the threads about Australia. What does anyone think about London? Australia is a British colony after all.
tommigun: You've read very old threads =)""",
409784: """Friend on a business trip in Japan:

Zomb1e: today I learned that the control panel of a modern Japanese toilet has 14 control buttons, 2 of them double
Zomb1e: I really feel like Conan the Barbarian
Zomb1e: although deep down I'm somewhat offended at the toilet for not saying hi to me and not saying bye""",
409785: """ЕВА: after it my one and only update led to the fall of the system
Hett: to the instrumental?""",
409786: """Fluffy: come on to the sirens, get off tetris
Wicked: don't want to
Wicked: let me build at least something in my life
Wicked: properly
Wicked: at least a line""",
409787: """(a serious site about dishware,
visitors' questions section, official answers)

Question: Burnt the jam. Can anyone suggest a radical method of washing off sugar charcoal without chipping the enamel?

Answer: Sit your husband across from you, give him this text: "Darling, I will take you to the very edge of the universe... I will give you this star... With unfading light it will light our path into infinity"... and go""",
409788: """The founder saddled me (the CEO) with his nephew as commercial director as a social burden. The guy at the very first meeting declared himself the tribe chief of the company, explained to the employees that he's smarter, cooler and generally better than everyone present. The next day my talented designers made him business cards with the title "kammercial director", which he's been handing out at important meetings for a month now.""",
409789: """xxx: apparently, our housing utilities were included in the committee for preparing figure skaters for the 2014 Olympics in Sochi
ххх: looking for talents on the city streets""",
409790: """amazonka: how I love it when my English acquaintance compliments me
amazonka: in English everything sounds not like in Russian, all beautiful, elegant
amazonka: yesterday he told me he adores me because I'm such a Russian crazy
thin: well I constantly tell you too that you're fucked up""",
409791: """xxx: you know how a resume ends?
xxx: WITH LOVE, Alexander""",
409792: """xxx: Everything was so romantic: dim light, candlelit dinner
xxx: And then they turned the electricity back on and he sat back down at StarCraft 2""",
409793: """Лена: I had so many "New folders" that I created a folder named "New folders\"""",
409794: """ххх: yandex news: "Dmitry Medvedev unexpectedly arrived in Vladikavkaz"
ууу: he was probably driving home by his new GLONASS navigator...""",
409795: """xxx: Tomorrow's the most dangerous exam, and I haven't really read anything...
ууу: Don't lie. You definitely thoroughly read the thread about lineage 2 problems :D""",
409796: """xxx: and God said: I am Ax and Bx.
xxx: and made mov Bx, Ax""",
409797: """X.W.X 17D Angelianna: went off to work
X.W.X 17D Angelianna: damn, wrong place
URA 58ARM Meraam: going to the wrong place to work - that's a party that went well""",
409798: """an email from a female student.

Good evening Alexander Alexandrovich!!!You gave me some difficult term paper topic, there's nothing on the internet.....
what should I do??????""",
409799: """xxx: Yesterday a friend ordered a sliding-door wardrobe, when they brought it, it turned out there weren't enough doors (
yyy: So they brought him a platzkart wardrobe.""",
409800: """xxx: I, by the way, registered on Twister yesterday ^^
ууу: Give the link. To the circle.""",
409801: """xxx: I called you, why didn't you pick up?
yyy: Was at the cop shop.
xxx: what were you doing there?
yyy: Proving that it wasn't me who attacked the gopniks, but they attacked me after all.
xxx: what?
yyy: Well it's such an unusual story...
yyy: We went to the aquapark yesterday. But I wasn't planning to go back home after. So I threw the bag with all the wet crap (shorts, towel) into Max's trunk. And this evening went to pick it up.
yyy: And on the way back home two guys approached me, wanted something strange.
yyy: And everything in the bag had frozen over the day in the frost into an ice lump of a strange shape...
yyy: Basically, one of them ran away, but the other one - traumatic brain injury.""",
409802: """<SergeyHit> People, Yandex has been searching for three minutes already, what do I do, help?!!
<KingMedved> Wait. He's googling.""",
409803: """Tsubaki: I don't even have the mood for depression...""",
409804: """I'm so unlucky at cards that I can't even lay out a solitaire :(""",
409805: """xxx: And after the math faculty where do they take you to work?
yyy: to the factory, to turn integrals on a lathe.""",
409806: """From habr comments on the opening of the WikiLeaks online store:
...
State secret of France — 100 000 $
State secret of England — 1 000 000 $
State secret of the USA — 10$ per hundred
...""",
409807: """xxx: Did everything you said - kissed her, asked her out. She agreed. What do I do with her next?""",
409808: """Discussion of Fadeev's interview with Navalny:

xxx: Fadeev's words: "In 5-10 years we'll live better than in Germany, no doubts about it." I didn't get it, what do they want to do to Germany?""",
409809: """xxx: and my wife beat all records, giving me a jewelry store gift certificate for February 23""",
409810: """xxx: 100 kg - weight taken
yyy: is that bench press? :)
xxx: no, that's stepping on the scales :(""",
409811: """JDS: the little one's phone got stolen at music school. I'm scolding her:
- why didn't you hide it?
- well, I didn't know that thieves also go to art school!""",
409812: """A girl's VK status:
"Write me what you erase before sending."
Comment:
"Ghbdtn\"""",
409813: """from guns.ru, a topic about selling a cat

xxx: Does this trio have bells?
yyy: No, the cat is neutered""",
409814: """<Osaka> A United Russia deputy stated that Runet has been bribed by the USA
<braintorch> damn, when was I bribed? Why didn't I notice? Where's my money, after all?!
<DarkElve> seee
<DarkElve> I'm confused too
<DarkElve> I'm bribed, but the bribe wasn't delivered
<DarkElve> how's that?
<Лэн> I think it's a consequence of sawing in the US State Department! The funds allocated for maintaining the opposition in Runet were squandered by officials on porshkaiens. Navalny should look into this!""",
409815: """ForsMajor: how do I bring a girl to my apartment, my bathtub isn't finished?
Полярник: you haven't seen her apartment )
ForsMajor: unfinished renovation there?
Полярник: unfinished demolition of the house there.""",
409816: """xxx: City duma elections soon. Got a job as an agitator, going around apartments, distributing newspapers, flyers...

xxx: Finishing today's rounds, going into the last apartment, a guy opens the door, all perplexed, holding a herring in his hand. Asks: what do you want?! Well, I start pouring him the newspaper, flyer spiel. He looks at the newspaper for a few seconds, with a look like he's thinking: how far to send me. Then shifts his gaze to the herring, lit up all over, grabs the newspaper from me, wrapped the herring in it and so heartfelt: thank you, girl! you're just in time!

xxx: for once I felt my work was needed by someone )""",
409817: """MaximAL:
— Do you speak English?
— Only from my heart!""",
409818: """ak_rafa: It's very dangerous to meet a woman who fully understands you. It usually ends in marriage.""",
409819: """xxx: and anyway, you and I are in the flower-chocolate period right now, so I should know which flowers and candies you like
xxx: otherwise it'll all start with a storm petrel and red carnations""",
409820: """*** ***ев added the app What do your friends think of you?
...
*** ***ев is upset
*** ***ев you bastards :'(
*** ***ев won't go to the Friends Meeting on Friday
*** ***ев added the note "Bastards\"""",
409821: """xxx: During the day I got about fifty calls; these calls can be divided into three groups:
1. Calls from your mom
2. Threats
3. Threats from your mom""",
409822: """ххх: my ex-girlfriend was studying to be a psychologist, helped my friend get out of depression
ххх: a year after the breakup I learned how (""",
409823: """xxx: Today before my eyes they saved a boy of 9-12 years old's life.
xxx: The salespeople in the video game store talked him out of buying WOW.""",
409824: """Vasya: Many are trying to prove: the internet is evil, you're all addicted to it...
Vasya: They understand nothing...
Vasya: I got married thanks to the internet.
Vasya: Nothing would have made me go to the cinema if they hadn't turned off the internet, there I met my future wife.
Vasya: Hers was also turned off then for non-payment :)""",
409825: """Gvinevra: Bought it?
Nee-kun: No, I was hurrying home to cook dinner for Masha in time.
Gvinevra: Oh, can I be your Masha?
Nee-kun: Come on. Your Ilyukha also cooked you something yesterday. What did he cook? )
Gvinevra: Really? No idea
Nee-kun: Then it's not for you...""",
409826: """i_one_as: we moved to a new office
i_one_as: someone for some reason stuck a leaflet on the window with "help" written on it
i_one_as: across the street is the Thailand consulate building
i_one_as: they called us, asked if everything was OK with us""",
409827: """xxx: Man the Higher Mind endowed with a more valuable instrument than body mechanics, namely - the ability of a more subtle perception of its (the mind's) vibrations.

yyy: I won't ask how it's known that some Higher Mind exists. I again won't ask who endowed this Higher Mind with properties surpassing human ones. I'll only ask - how is it known that it vibrates?""",
409828: """xxx: brought my work to the prof today
xxx: he looked through it, put it aside and says to me: "I'll give you a pass now, but promise me, please, that you will never work with rebar\"""",
409829: """markermeer: And of course Immortal comes to mind, especially their last clip of August 2010, when the guys stand high in the Norwegian, as I understand, mountains, snow all around, ravens flying
Serpent: You just described ALL of Immortal's clips""",
409830: """Ххх: Loneliness is when you often stay one on one with pelmeni.
Ууу: One on one with pelmeni is not loneliness. It's happiness!!!
Ххх: well, depends on which side )
Ууу: Yeah, for the pelmeni it's, of course, mourning...""",
409831: """xxx: Decided to please the cat today.
xxx: Little, damn, joy in his life - poured water into a saucer, dripped valerian in there.
xxx: Not only did this bastard get drunk as a pig, he also smashed the saucer =(""",
409832: """Санечка: no, went to the hospital with my daughter for a checkup
КИРЯ: What's wrong with her?
Санечка: nothing, with babies they go every month
КИРЯ: Maintenance?""",
409833: """Планетянин: wrote everyone to log out of Accounting, I'll be updating the program
Планетянин: two won't log out, I'm calling them - silence
Планетянин: and to update the prog I need everyone to exit the program
Планетянин: and they're standing in the corridor, chatting away - they logged out of accounting
Планетянин: out of the office""",
409834: """<Shadow> (humming)Kind traveler enter the glorious city of Baghdad,
<Shadow> you won't believe your eyes,
<Shadow> A cascade of adventures awaits you ahead,
<One Man Army> occupation, oil and Saddam.""",
409835: """xxx: And our calculus prof has very rich facial expressions. For example, at some elegant transition in a theorem proof he often says: "Now we'll do an ear feint" and DOES AN EAR FEINT!""",
409836: """xxx: who grew the most vegetables?
yyy: blizzard""",
409837: """URGENT! HELP NEEDED! In Solntsevo in a basement mosquito larvae are freezing, about 3000 babies. Help who can, take them for yourself or at least for foster care! They are very cute and affectionate. Eat everything.""",
409838: """From the discussion of the Egyptian events on Lenta.ru
xxx: ... for example, the Great French Revolution of 1789 and similar ones, when a democratic system was established. ...
yyy: As a result of the French Revolution first the Jacobin dictatorship was established, and then the dictatorship of General Bonaparte. Specify which of these regimes you call democratic?""",
409839: """(from a forum)

xxx: Little can scare me after that time when our turtle got stuck under the crosspiece of a chair on wheels, and came out with it from a dark corridor.""",
409840: """xxx: What would you like for for March 8th?
YYY: would like an apple... bitten... and preferably on the back of a new phone....""",
409841: """xxx: March - the calm before the season starts.
xxx: Cupid is practicing on cats.""",
409842: """Gissen:
Yesterday was in Moscow. On the way back I got lucky - a beautiful girl was riding in the same compartment as me. Oh, it was an unforgettable night! She screamed so! Almost all night!!!
The problem is that she was with her parents, and she was 9 months old )""",
409843: """xxx: Lyosha, are you an oceanologist? So you study fish?
yyy: Of course fish! Oceanologists study fish, meteorologists birds, and soil scientists earthworms.""",
409844: """Сабж - parachute jumps.

yoker: and we had one lady take her little dog with her on a jump, a tiny decorative one.
During free fall a beautiful trail stretched behind them.
One thing I don't get - how did twice as much shit fit into this dog as the dog itself...""",
409845: """from habr:
xxx: For some reason everyone remembers the war and the partisans, but no one thinks that before the sovok we also had police, not militsiya.
yyy: And the tsar. Everything just suddenly fell into place for me.""",
409846: """news:
The regional authorities need to find 2.3 billion rubles to complete the ring road around Belgorod.

Construction of a new section of the bypass road around Belgorod Tavrovo – Solomino - Razumnoye may cost the region more than 2.7 billion rubles, 413 million of which have already been absorbed.
...
The road's length will be 11.3 kilometers, this section will be the completion of a large-scale project to create a full ring bypass route around Belgorod, designed to relieve city streets.

The only comment:
"2.7 billion for 11.3 kilometers of highway. Divide, comes out to about 239 million/kilometer, or 7.9 million dollars per kilometer. One kilometer of subway costs 5 million dollars. Let's build 18 km of subway in Belgorod with this money, and everyone will be happy!!! These traffic jams are getting old...\"""",
409847: """Habr comment. Discussion of a program for tracking periods.

ххх: Add profiles to the functionality, so you can calculate cycles for several girls and it'll be a hit. A hit among boys =)""",
409848: """from Habr:
ххх: Any joke about Schrödinger's cat can be funny and unfunny at the same time!""",
409849: """xxx: but now I'm downloading the Countess de Monsoreau )
yyy: how many-th is this character of yours?""",
409850: """From Mail answers:
она: How do I create my world? I press create and nothing happens. Tell me
он: well first you need to separate the sky from the firmament and say loudly: "Let there be light!". Then everything according to the manual.""",
409851: """xxx: Life is shit.
yyy: Beware of fakes!""",
409852: """Today a friend changed his license.
Я: They say there'll be new licenses, with a barcode. Like in stores it'll be - they stop you, scanner - beep - you may proceed )
Друг: Yeah. Do you have our discount card? Need a bag? xD""",
409853: """xxx:
One citizen here submitted a resignation letter: "I ask you to [fire] me at my own will." Director's visa: "Noted\"""",
409854: """xxx: The best guide to action is your guide that got its ass kicked for your screw-ups by its guide.""",
409855: """<xxx> So here you sit, while they broke into my pepelats and cut out the gamnitola, instead of taking it out and disconnecting two wires.
<xxx> Fixing the car costs more than the gamnitola.
<xxx> Drug addicts, fuck.
<yyy> what do the police say?
<xxx> They say the offense was committed in February - go to the militsiya.""",
409856: """Kumo_kinder: On the news portal: "A US, British and French landing force has landed in Libya, - media"

Comment:
God, how good it is that Ukraine has no oil...""",
409857: """Ирра: I washed my car yesterday, instead of a tank I now have an executive sedan :)""",
409858: """from Wikipedia:
Бука Сука Димка (Buka Suka Dimka) — lieutenant colonel of the Nigerian army, organizer of the February 13, 1976 coup d'état.""",
409859: """Артём: *slowly takes off his robe, then fur coat, tights, felt boots, erotically throws his ushanka hat aside*
Артём: I'm all burning!
Катя: burn in hell, you animal xD""",
409860: """ear: The prof, a grandpa of about 70:
- Four years married? And no kids yet? What are you, excuse me, dragging the rubber for?
And wild laughter from his fellow profs of the same age.""",
409861: """xxx: Hellboy, Hulk and Avatar are an excellent visual representation of the RGB model.""",
409862: """bitoniau: Sometimes car dynamics are affected not by the engine, transmission and other smart stuff, but by a burnt-out handbrake indicator bulb""",
409863: """Ж: I spotted these sandals for 4.5 grand!
М: are they winter ones or what?""",
409864: """Been asking mom to get some animal for a long time. From a recent conversation:
(pointing at a chinchilla photo)
- Mom, what if I bring home such a cutie, what will you do?
A laconic answer followed:
- A hat.""",
409865: """xxx: And I love fishing, but I don't know how.
yyy: What's there to know? Pour and drink.
xxx: That can grow into tourism.""",
409866: """xxx: Yesterday the prof told us how one girl defended her thesis. She'd swallowed strong sedatives beforehand. Walked into the room, said hello, stood a bit, said goodbye and left.""",
409867: """xxx: what I liked most about the new iPad was the price drop on the old one""",
409868: """xxx: Zhenya, you're a gopnik!
yyy: It's called casual!
xxx: Within the Boulevard Ring it's casual, but in Mytishchi you're a gopnik!""",
409869: """Дартаньян: Damn, my boss at work sent me :(
Печальный ежиГ: for suspension? :D""",
409870: """xxx: cats eat either something tasty or something healthy
yyy: and what does tinsel belong to?""",
409871: """Ad on one of the sites:
Selling a used diving suit size 52 6.5mm 500 UAH. Reason for selling - need to eat less.""",
409872: """Two programmers:
01: One more awkward move on your part...
10: And???
01: And... your awkward move counter will increment by one.""",
409873: """xxx: Listen, what if you first drink a bunch of energy drinks, and then sleeping pills, who wins?
yyy: Darwin wins""",
409874: """Сергей: done
Сергей: helmet report
Slava : cuirass report
Slava : greaves report""",
409875: """status in ICQ:
I know the password, I see the ATM, I think my husband won't be happy in the morning""",
409876: """LEX: do you know what WoW is?
nyanka: two beaks fighting over one grain?""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 701, 800  # 1-based line numbers
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
