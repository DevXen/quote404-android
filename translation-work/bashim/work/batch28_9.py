#!/usr/bin/env python3
"""Append English translations for bash.im chunk 28, records 801-900."""
import json, os

BASE = '/home/hatch/workspace/translation-work/bashim'
srcp = os.path.join(BASE, 'chunks/chunk_28.jsonl')
outp = os.path.join(BASE, 'out/chunk_28.jsonl')

EN = {
410879: """Alpha: by the way, were you wearing those new jeans today that you've been telling me about for three days?
Lightbrinder: yeah. how do you like them?
Alpha: well... as soon as I saw them on you, I immediately wanted to take them off you...
Lightbrinder: mmm. exactly the effect I was going for.
Alpha: ... then tear them into small pieces and BURN THEM TO FUCK!""",
410880: """ххх: Sanyok, I accidentally hit the cable with the chair leg, it seems to have torn, sorry.
ууу: The admin will forgive.""",
410881: """WakeUpHate: Through more, through less we wander to the land of wonders.""",
410882: """On Habrahabr an article about how to make a doorbell in the shape of a mouse (using an old mouse).
komjah: and on double click the door opens? :)""",
410883: """Хатаруъ: on weekends the granite of science is hard as never""",
410884: """xxx: What a mess on my desk? Can't even put my feet anywhere
yyy: Yeah. When I come to work I also immediately clear it
zzz: With what? Your feet? )
yyy: Well I'm not going to touch this dump with my hands""",
410885: """xxx: Today I saw an ad: new Fairy became 2 times thicker again.
xxx: Every year for 10 years it keeps getting thicker and thicker..
xxx: When will it petrify already, fuck?""",
410886: """xxx: I found out there are healthy and harmful calories
yyy: Nikoletochka, calories aren't healthy or harmful, foods are, calories aren't
xxx: There are! I read a book so don't. Like in chips they're harmful and in a kilogram of apples there are just as many but healthy.
yyy: Calories are a unit of measurement, that's one. They measure ENERGY, that's two. I repeat, only the foods themselves can be harmful.
xxx: But foods consist of calories!
yyy: FOODS DON'T CONSIST OF CALORIES! THEY CONSIST OF FUCKING MOLECULES
xxx: and molecules consist of calories""",
410887: """xxx: No, he's a fine guy, only his life stories kill me.
xxx: How do you like the phrase "Yeah, yeah, in the nineties I remember we didn't split a Kamaz of sand with a friend, there was such a massacre"?
yyy: So what? Times were like that then.
xxx: And nothing that he was born in '88 and the Kamaz was plastic, with sand from the kindergarten sandbox?""",
410888: """xxx: Who would've thought in youth that the skill of weaving chainmail from aluminum wire would help me sew fishnet stockings!""",
410889: """Марат: I'm Schrödinger's cat, until you come to volleyball tomorrow you won't know if I'll come
Саша: And I'm Schrödinger - and until no one knows whether you'll be alive tomorrow if you don't come""",
410890: """<в общаге>
xxx: My socks disappeared somewhere..
xxx: And the new shower gel I've been looking for 2 days..
xxx: Even the cockroaches haven't been seen in a long time!
yyy: Wait, they'll wash them, they'll bring everything back themselves)""",
410891: """ххх: And some don't believe in magic. Since I became department head, the whole office turned into sheep before my eyes.""",
410892: """character йух banned, reason: йынткеррокен кин""",
410893: """Today at work a colleague summoned the admin due to some computer problem. The admin came, important, in glasses, sat in the colleague's chair, held a Stanislavski pause and pronounced: "Well... Brag!\"""",
410894: """ViToni: Today conducted trainings with kids at school (1st grade). The kids were drawing, when I asked to give me the drawings, one boy plaintively said - "Take care of my drawing like your son...", and a girl next to him added "and take normal care of my drawing, please!\"""",
410895: """xxx: Look, a guy at a lecture reads a report, reads long, reads boringly (while mangling unfamiliar words)...
xxx: ...so he's almost read it to the end...
xxx: Guess what the last words in his report were?
xxx: "...source not cited for 68 days.\"!""",
410896: """A girlfriend tells:
xxx: I told Igor: "Choose! Either I'll go to clubs or I'll get a tattoo" And he said... he said... he told me to fuck off!!! (((""",
410897: """xxx: I feel a bit weird when I read political news and laugh""",
410898: """xxx: I took Alyonka to the gym for 3 months, now she beats me with those bras that are now too big for her""",
410899: """она: So much time has passed, and you're still angry at me, come on?!
она: After all there was something good for you in me leaving you for another? )
он: Of course, on the very first day at the gym I earned the respect of two coaches at once, in kickboxing and in hand-to-hand, when I worked the bag imagining you""",
410900: """A Chinese citizen named Mao Li lived for a whole year in a concrete pipe almost two meters long. Inside he made a wooden "floor" and door. Of household goods only a pillow and a blanket. He lived like that for a year, until the authorities noticed him. "We must not have people living in pipes" they said and promised to give him an apartment...
xxx: And our authorities wouldn't even notice...
yyy: And ours would say "We must not have people living in pipes" and take away the pipe""",
410901: """On a music forum:

Hendrix: I want to learn guitar, recommend a good book

Reggae: I could, but when I look at your nick, my hands drop, I can't recommend""",
410902: """We have a small shop in town "for our own" - no ads, modest sign, good prices. Built by a neighborhood resident for neighbors, because other stores are extremely far and inconvenient to drag to, and here everything is at hand. Almost like Killian Darkwater - in the Hub everything is there, but where's the Hub and where are we.
Mostly our own go there - strangers don't know about this store, so they stand out against the natives. I go in, see some "stranger" customer discussing with the seller (the store owner himself). Either they brought him the wrong thing, or something else. And I hear the sacred phrase:
- I'll complain to your management!
- I AM the management! - the owner briskly retorts.
Without batting an eye the customer blurts:
- Then I'll complain to your wife and she'll deprive you of draniki!""",
410903: """In the zakupki-news community a post about FSO boat procurement. Photos of models shown. On the upper decks all girls.
eagleas: Do the boats come with chix included?
tnkr: The chix coating on the boat surface forms during use.""",
410904: """grother: how to explain to a child that mounting foam and corn sticks are not the same thing?""",
410905: """х> How to do the right thing?
y> act purely like a man, shift all responsibility onto her, do as she wants, and then say she wanted it herself!""",
410906: """xxx: We have such intersections in our district that "go right - get fucked up, go left - go right" =/""",
410907: """xxx: since I showed mom the sims, I've become an orphan.""",
410908: """Фуфырка (17:34): you wanted to leave at 16:30...
Елена (17:35): I still want to""",
410909: """ххх: I have an acquaintance here. When he thinks about what's wrong with the code or how to add something else - he sits on a backless chair (he has one specially by his desk at home) and does crunches - hands behind head and leans back, then rises, and leans back again.
ххх: Everything would be fine, if he hadn't spaced out and at work (sitting on a swivel backless chair) hadn't started doing the same thing. At this time the boss walks in with some inspector saying: "and this is one of our best employees..."
ххх: and they silently watched for 5 minutes as "one of the best workers" rocks on the chair, and then with a shout "HALLELUJAH!!!" rushes to write code...""",
410910: """xxx: now I'll throw a stone into your garden too
yyy: thanks, no need, I already have Stonehenge at the dacha :)""",
410911: """atlas air: annoying people who want to buy "More" cigarettes and ask the saleswoman:
- Do you have blue "sea"?""",
410912: """xxx: Yesterday arrived at the car wash, handed over the car, took an air rifle out of the trunk that I bought as a gift, and periodically glancing at the work of the washer-jamshuts, was figuring it out.
xxx: My car has never been washed so well anywhere.""",
410913: """ххх: What to do?
ууу: do good
ххх: nah, boring (
ууу: then evil
ххх: also not an option
ууу: fuck, make a birdhouse""",
410914: """Nar: Tell me, what makes up the price for developing a banner layout?
mesc: the designer's greed and the client's stinginess )""",
410915: """Он: where are you now?
Она: in the next room, idiot!!
Он: then where am I?
Она in the next room
Он: are we in the same room or what?""",
410916: """xxx: for Sashka's birthday they gave him a funny gadget - a pen with a spy camera. guess what he and his wife did first
yyy: installed it in the bedroom? in the toilet?
xxx: missed. unscrewed all the extras and attached it to the cat's collar. yesterday watched the footage for several hours, and they're supposedly adults""",
410917: """natalisdomini@lj: And now many are already asking: if Gaddafi is guilty of ruling too long, why aren't the same claims made against the English queen?""",
410918: """xxx: Why so gloomy today?
yyy: Barcelona won, I didn't get enough sleep, my boyfriend is shit.
xxx: Seryog, don't you think she composed a blues?""",
410919: """Милая: and also: I really really don't like that you ride without gear.
Michael Scofield: And that we have unsafe sex means nothing? :)
Милая: unsafe sex makes people be born, and there it can have the opposite effect.""",
410920: """Message from unknown number: "Dad I got four fives today".
My answer: "Well done! Now learn Russian\"""",
410921: """ХХХ: I broke up with my girlfriend and now I have a manic desire to fuck some friend of hers )
YYY: when I broke up with mine, I fucked two of her friends.
YYY: one though even before we broke up
YYY: that's why we broke up actually""",
410922: """xxx: I have 2 summons for one date: to court and to the enlistment office.
xxx: So where do I go?""",
410923: """xxx: Don't be mad, darling. I really didn't mean to.
yyy: ...
xxx: Come on punish me at home) I'm a bad girl, I need to be spanked)
yyy: For this you generally need to be beaten up""",
410924: """ISO 8859-5: Sitting now in the office, boredom mortal, no one but me, everyone scattered to instances. Did everything possible. Phone rings. Decided to prank the citizen.
- hello?!
- hello, missile boat base, petty officer Aproksin speaking
- oh, fuck, sorry... beeps...
Hang up and realize the general director called.""",
410925: """он: you ruin my whole life...
она: why?
он: everyone's sure I have a girlfriend...
она: what do I have to do with it?
он: well it's you!""",
410926: """Anatoly:
I've long resigned myself to being called Nikolai and Alexander in correspondence, and even Anya a couple times. But today something terrible happened: I got a letter starting with the words "Dear Analtoy,". From a Dutchman.""",
410927: """xxx: And I think "bebebe" is a win-win argument in any dispute :)
yyy: No, a win-win argument is the question:"So what?"
xxx: I can say "bebebe" to your "So what?" too )
yyy: So what?""",
410928: """xxx: Which washing machine should I get, top-loading or front-loading?
yyy: Of course front-loading
yyy: Is your bathroom combined with the toilet?
xxx: Yeah.
yyy: There you go. You'll sit and watch it spin, since you've probably reread all the labels?""",
410929: """xxx: Don't wake Dimon
xxx: he drank his stipend yesterday
xxx: he'll be sad if he wakes up )""",
410930: """Камышовый_Кот: We tricked him on the hike ) We say this place is mystical. If you write what you want on a paper and put it under that huge stone, it'll come true. He's like: "nah, I don't believe, it's all bullshit and anti-scientific nonsense". And in the morning we found a small crumpled paper there. We unfold, and there in his handwriting: "Want a chick. Definitely with tits.\"""",
410931: """xxx: for some reason now my computer sees the iPod as a camera and offers to open it with Word""",
410932: """xxx: I remember, we had renovations and my sister kept turning on the screwdriver - buzzes and turns off.
xxx: I turned away somewhere, and she turned on the screwdriver and poked my back with her finger, I screamed so much, barely stayed dry""",
410933: """- Mom, do we have rubber gloves?
- In the bottom drawer, why do you need them?
- Well my jeans button tore off
- ???
- Where do you think it fell?""",
410934: """Доктор911 (15:17:21 28/04/2011):
I'm certainly no aristocrat, but it's not morning anymore""",
410935: """ххх: What's for dinner, darling?
ууу: rolton, because darling cooks like shit.""",
410936: """ххх: Scientists managed to erase a sea snail's memory
Isn't that what war veterans, rape victims and other people who witnessed something horrible dream of?
ххх: hmm. I think that's not quite what they dream of.
ххх: I think not all veterans dream of the snail having its memory erased""",
410937: """ххх: I study a couple steps from Red Square. Before the exam on 20th century Russian history I studied this history all night, prepared. Terribly tired, in the morning my head doesn't work at all. I go to uni, get out of the metro, out of the corner of my eye I notice Lenin coming out of the metro with me. Quite alive, squinting in the sun, smiling at me. Well, I think, trouble. Overstudied. And he lit a prima and went to take photos with tourists on the square.""",
410938: """[Purpleblood] and I have joy =3
[Purpleblood] I was given an electric guitar
[Ahalay] gorgeous
[Ahalay] you'll be like my asshole neighbor
[Ahalay] playing all day (=
[Purpleblood] I can't play
[Ahalay] neither can he
[Ahalay] bastard""",
410939: """xxx: examining a stiffened and slightly gnawed corpse.
xxx: three not-small dogs clearly hint that they found it first and don't open your mouth at others' goods...
xxx: after the phrase addressed to them: "stop barking, tell us what you saw" they instantly disappear behind snowdrifts and don't appear again.""",
410940: """Dero: Let us praise the lord that for Ctrl+C - Ctrl+V combos you don't need to switch keyboard layout. For others, of course, also not needed, but for these two - let us praise.""",
410941: """My desk at work is two meters from the door, facing the entrance. Anyone who even just opens the door first sees me.

Today at noon.
The door sharply opens, behind it stands a characteristic-looking young man in a leather jacket, sweatpants and a black knitted hat. Possibly (I'm an optimist), a student of the physical education faculty. My first thought: "Who let this gopnik in here?" Asks:
— Is this a cafe or what?
I look at him, changed in face.
— It says here [nods toward the sign] "Cafe"...
I keep looking at him.
— A-a... [looks at the sign] cafe... dra...
Leaves.
The sign on the door reads: "Department of Theoretical and Nuclear Physics\".""",
410942: """Алчи: Enthusiasm is off the charts. Last night went out to the balcony for a smoke, saw on the store roof an oblong green glowing object. Burns steadily for a minute, then for 4 seconds flares brighter and pulses. I watched it for a long time, built hypotheses about what it could be, then went to the garage for a ladder, turned the surveillance camera away with a stick, put the ladder to the store roof, fell, badly injured my leg, hit my head, on the roof ran into two drunk comrades who also climbed out to see what the hell it was. When we found it (at 1 AM), five people from balconies asked what it was. Apparently also interested in the unknown green glow.
Dron: And what was it?
Алчи: A glass stick with an LED and a battery. I'd like to know what bastard threw it up there.""",
410943: """script: I must've been dreaming something very interesting if in my sleep I took the pillowcase off the pillow""",
410944: """Account manager: Good day! Did you manage to review the estimate presented?

Client: Yes, I opened it, shit myself and closed it. Not ready to discuss this now.""",
410945: """Лиска:
talk to me...
let's talk about something soulful

Кот:
fuck... well you picked a great topic!""",
410946: """FeDя:
Lying in the hospital, I'm scared here, went to the toilet today, there's blood in the toilet and 3 screws lying... Don't know what to expect next, can't sleep, lying with a fork under the blanket...""",
410947: """Larik: network of 15 computers
Larik: no server
Larik: no router
glendor: populated with viruses""",
410948: """xxx: In the history of modern Russian show business I'd highlight two turning points - first, when hairdressers started calling themselves stylists, and second - when they started singing.""",
410949: """News: "According to Obama, bin Laden's location became known last week, and on May 1 he was killed. During the special operation none of its participants were hurt."

xxx: Yeah, apparently bin Laden declined to participate in the operation""",
410950: """Google Q&A:
Q: What other movie is there in the style of "Transformers"?)
A: IRON MAN 1, 2
TERMINATOR 1, 2, 3, 4
ROBOCOP 1, 2
Kin-dza-dza
Transformers 2
Well, Just You Wait (episode 14)""",
410951: """the other day a neighbor came in and saw our catdog:
- wow what a cutie, let's mate them! my cat is in heat right now, let him come play.
well why not. let the catdog in to her, 15 minutes later a call:
- take him back.
- uh.. already?
- he's fucking my cat!
- and what did you want?
- I thought they'd play and that's it, but he grabs her by the scruff with his teeth and fucks her!""",
410952: """roizman@lj:
Called from some fashionable gastronomic magazine:

- Evgeny Vadimovich, we're conducting a poll: Can you remember which alcoholic drink made the strongest impression on you?

- What is there to remember, - I answer, - port wine "Agdam", at Uralmash, behind the fence of the sixth vegetable warehouse in 1976!

And I realized they were apparently expecting some other answer from me.""",
410953: """xxx: sitting with my brother in one room and talking on the phone with a guy
xxx: I tell him: "My bunny, I love you"
xxx: my brother jumped up and to the whole apartment: "Mom! Why does Olya have a bunny, and I can't have a hamster?!\"""",
410954: """GK: Now the magnificent samurai swords, forged in the Fukushima district, have received additional radiation damage.""",
410955: """xxx: what do you think, will my anonymous sysadmins club be a success?
yyy: "Hello, my name is Vova and I'm a sysadmin for 7 years".
yyy: in chorus - "Hello Vova".
yyy: "I haven't patched under FreeBSD for a month, shaved my year-old beard and threw away the old reindeer sweater".
yyy: "You're on the road to recovery! Here's an anti-stress floppy disk, break it as a ��nak of your steadfastness!"
yyy: in general, I'm your first client)""",
410956: """111: my computer is glitching, what to do???

answers:
222: my car won't start, what's the reason?
333: want to lose weight, but can't, what to do?
444: I quarreled with my wife, don't understand why?
555: boiled pelmeni, turned out tasteless, why?
666: I got sick, how do I get cured?

111: are you idiots or what? this is a forum about computers not for stupid housewives like you!!!! I need a concrete answer to a concrete question!!""",
410957: """From eprst2000's LJ:

When I come home from a shift, mom asks: "How was your day, daughter?" She has this indescribable trait, which she probably borrowed from American movies. It's when the hero lies with torn-off arms and legs, his guts wound around his neck and his own liver sticking out of his mouth, and he's asked: "John, are you okay?\"""",
410958: """111: according to an ancient Japanese legend, a person who folds 1000 paper cranes can make a wish that will definitely come true!
222: according to an ancient Russian legend, a person who folds 1000 paper cranes can get fucking tired.
111: in practice, stretching from ancient times, a Russian person can get fucking tired on flat ground""",
410959: """t00r: was at a personal growth training. turned out to be a sect
t00r: it was fun and free
t00r: then they wanted money and for me not to drink in the auditorium""",
410960: """xxx: I don't like Nokias for just one thing - why every evening when I set the alarm do they tell me I have 3 hours of sleep left?!""",
410961: """<Graf> You know, I'm a programmer. If the shed says dick, there must be a dick there. In the extreme case - a pointer to a dick.
<Graf> If there are firewood there - then some faggot redefined the type "dick\".""",
410962: """<Васян> In the game Portal the main heroine Chell ran in an orange jacket. In Portal-2 she took the jacket off and now runs in a white tank top. Imagine, huh?
<Чучундра> Can't wait for Portal-3?""",
410963: """<forgotten_dream> oh come on
<forgotten_dream> May 1st is cool
<forgotten_dream> I remember being little I went to demonstrations with dad
<forgotten_dream> there was a lot of lilac
<forgotten_dream> everything was green, sun
<forgotten_dream> I was on dad's neck
<forgotten_dream> basically nothing has changed
<forgotten_dream> I'm on dad's neck)""",
410964: """welshowski: My biology teacher couldn't even say the words "ovulation", "clitoris", "penis" without a shot.
alexpaknix: What interesting relations you had with the teacher!""",
410965: """xxx: And you don't regret that there's one less prince? :)
ууу: Everyone says: a prince, I want a prince. And I'll marry a modest revolutionary. And that's the end of these princesses""",
410966: """Она: You know, I so regretted at the last move that I didn't buy that bag for 3000 rubles.
Он: What happened?
Она: Well it was so comfortable, roomy, well made.
Он: Something must've died in the forest.
Она: It just had such a cool color.""",
410967: """aaa: You don't happen to know what unit of measurement wtf/m is?
bbb: I know. Measures the quality of programmer code. What's the fuck per minute.""",
410968: """Lotta: Went to my boyfriend's for the night, in the morning mom calls and says: Call grandma, just say you're home and doing homework!!!""",
410969: """xxx: Dude, hi. Do you code in Visual Basic?
yyy: no way
xxx: need to do simple first-year uni level tasks. for money
yyy: even for money
xxx: what, did you all conspire!""",
410970: """xxx: Where to buy a one-sided coin?
xxx: For the third week in a row I keep getting "don't go to uni\"""",
410971: """Katezlo: Burn in hell, heartless faggots, who put a wet umbrella on a trolleybus seat.
Katezlo: You have no soul, but I have a wet ass.""",
410972: """xxx: zzz recently returned from the army, should meet up with him, see him
yyy: since he came he's just been drinking, they taught him there
xxx: taught him? well let him come, show us!""",
410973: """I work with printed circuit boards. Once on the way to work I realized that for the May holidays I need to rest properly. While waiting at the traffic light, I glanced at the stripes of the pedestrian crossing of an X-shaped intersection and my first association was that it's a footprint for a BIG planar microchip.""",
410974: """xxx: sitting now, flipping a notebook and thinking, why doesn't it have a table of contents
xxx: with hyperlinks""",
410975: """xxx: Do you have lead?
yyy: can find, why do you need it?
xxx: want to make nanogiri.
yyy: they invented nanogiri already?
xxx: Na nogi giri*....""",
410976: """Светлана: you knew what an amazing apricot cupcake is lying on my forge right now
Светлана: and I'm not eating it, because my goal is 45 kg
Дарья: AAAAAA
Дарья: I knew it!!!!!!!!
Светлана: what? (
Светлана: that I weigh more than 45 kg? (
Дарья: I knew you have a forge at home!!!!!!!""",
410977: """xxx: Don't know what to write a girl?
xxx: Write her a term paper.""",
410978: """XXX: BUG constant check is required here because of architecture proёb
YYY: Yeah, that place isn't fixed solely because of the well-formulated comment""",
}

def main():
    recs = {}
    with open(srcp, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                r = json.loads(line)
                recs[r['id']] = r['text']
    assert len(recs) == 1000, len(recs)
    for k in EN:
        assert k in recs, k
    cur = 0
    have = set()
    if os.path.exists(outp):
        with open(outp, encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line:
                    cur += 1
                    have.add(json.loads(line)['id'])
    with open(outp, 'a', encoding='utf-8') as f:
        n = 0
        for i in sorted(EN):
            if i in have:
                continue
            f.write(json.dumps({'id': i, 'ru': recs[i], 'en': EN[i]}, ensure_ascii=False) + '\n')
            n += 1
    print('batch28_9 OK, wrote', n, '(output now', cur + n, 'lines)')

if __name__ == '__main__':
    main()
