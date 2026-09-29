#!/usr/bin/env python3
"""Translate bashim chunk_28 records 101-200 (input lines 100-199). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
410178: """xxx: The potato bag says "Natural Selection".
xxx: I'm kinda afraid to eat them — who knows what her criteria were.""",
410179: """ihaveabomb: with every new MS Office release the first thoughts aren't "wow, how convenient it's become", but "fuck it, we'll get used to it".""",
410180: """xxx: Never read an accounting textbook lying on the couch! You'll fall asleep and never wake up.""",
410181: """Mama: eat the pelmeni before it's too late
Me: I'll manage, what could happen to them
Mama: they'll go on a business trip
Mama: in papa""",
410182: """Odd: American democracy spreads via the air-and-missile route.""",
410183: """ххх: they're bugging me at work! what to do?
ууу: go sleep in the bathroom, one guy at my old job did that...
ууу: true, he later got hit by a car when he went out for a bun at lunch, but that's probably unrelated, though who knows)""",
410184: """During a driving lesson the instructor talks about proper driver behavior when the car skids, and asks:
- What's dangerous about bitumen on the road during road repairs?
A girl from the back rows:
- It doesn't wash out!!""",
410185: """Was checking trial USE papers in Russian language.
love my job, where else would you read this..
ххх: Prince Andrei Bolkonsky in Lermontov's novel "Quiet Don"...""",
410186: """xxx: you asked to rename in the database correctly Nakhodku, Korolev, Kurgan and Kovrov
xxx: we don't even have such cities in the database!
yyy: yeah, but you've got Find, Queens, Barrow and Carpets!!!""",
410187: """xxx: Fuck!
xxx: 6 of the best and most knowledgeable workers are quitting production — for the standard reason "you pay too little" (really too little, no kidding).
xxx: So management hired a PSYCHOANALYST to talk them into staying.
xxx: After a 10-minute speech the shrink was predictably told to go fuck himself, and left with nothing.
xxx: Where I work...""",
410188: """snatch: we had a guy come in applying for a PHP programmer job, well, our guys asked him one of the first questions at the interview: how well do you know PHP? to which he, without hesitating, answered: very well, only 6 pages left to read.""",
410189: """1: Good morny-morning )
2: hi! is "morny" some kind of seafood? like "shrimpy"? )""",
410190: """xxx: And I just noticed the other day that the computer noise sounds a bit like a fan noise
yyy: You won't believe this...""",
410191: """dema: Before that he worked as a surgeon-urologist in the forest
kavic: ahem.. well I understood we're talking about our city hospital #1, which is built in a forest belt, but the other chat participants, not living in Stary Oskol, are clearly puzzled right now
kavic: I'd even say STRONGLY puzzled""",
410192: """< lin> here's a resume for sysadmin/deputy head of the IT department.
< lin> judging by the resume the guy is a demigod
< Kaneda> well then hire him at half the salary""",
410193: """Office. The chief accountant (an intelligent woman of a certain age) says excitedly to the whole department: Do you know that Vanga predicted that the catastrophe in Japan is the beginning of the end of the world?
someone from the staff: you should watch less TV.
главбух: no, I just wonder whether to do the reporting or not...""",
410194: """I live in a building from 1963. A while back we did renovations and changed the toilet. If anyone's seen, in old buildings the flush tank is about 1.5m above the toilet and they're connected by a pipe. I'm carrying this whole construction out of the entrance, the grannies on the bench as usual. I couldn't find anything to answer their question: "What, are you moving?\"""",
410195: """(A conversation about how my kotte tore apart a toy mouse and brought it into my bed).

Она: Ew, a mouse, that's horrible! Why even buy cats such toys!?..
ds: What was I supposed to buy him, Lego?""",
410196: """Interview at a bank.
Рекрутер: Your resume says you haven't worked since last October. What have you been doing until now (March)?
Кандидат (на полном серьезе и с напором): Played Warcraft!""",
410197: """vlom: have you ever had mush in your head?
Brick: Yes. When I eat porridge.""",
410198: """xxx: Rosnano and Skolkovo have jointly developed the first Russian nanotechnology: the embezzlement rate increases thanks to recoil energy.""",
410199: """Sitting, drinking beer with company. Across from me sits a female acquaintance, who suddenly jumps up and rips the slipper off my right foot.
Says "weird" and, catching my stunned look, says: "I thought you put the slippers on because your socks are torn."
Everyone's smiling, I look at her with a condescending smile, thinking to myself "I just hope she doesn't think of taking off the left slipper!\"""",
410200: """DoubleDJ: Driving about 90 km/h, a DPS car comes toward me flashing its high beams.
DoubleDJ: Got to thinking, but just in case slowed down to 60.
DoubleDJ: And sure enough! Ahead on the roadside another DPS crew is catching people with the radar.
DoubleDJ: Warring clans? :)""",
410201: """From the news:
Famous Hollywood actor Sylvester Stallone announced the launch of his own clothing line, based on the looks of his own characters in action movies like "Rambo" or "Rocky"

Next year tank tops, bandanas and machine guns will be in fashion.""",
410202: """flapenguin: You know what it says in my niece's geometry textbook?
flapenguin: A triangle is called right-angled if _at least one_ of its angles equals 90 degrees.
flapenguin: At least one, fuck. Fuck you, Pythagoras.""",
410203: """xxx: techies who end up in hell will be doomed to rewrite "War and Peace" by hand in GOST lettering.""",
410204: """fals89: Fuck.
fals89: I did suspect that before putting the down jacket in the washing machine, I should have sewn up the holes...
fals89: But now I have a stylish windbreaker.""",
410205: """ххх: today at work I was explaining a phrase from the news feed to a young lady:
"coalition aviation continues to strike Gaddafi's positions".
ххх: destroyed her inner world completely.
ххх: she learned today that a coalition is not an organization for protecting koalas. And that the coalition can quite well have aviation.
ххх: but I think she didn't fully believe it until the end)""",
410206: """The Norwegian Nobel Committee has decided to henceforth award the peace-strengthening prize in TNT equivalent.""",
410207: """xxx: ahhh lol
yyy: I suddenly pictured a medieval lady who, blushing lightly and fanning herself with an elegant fan, breathlessly pronounces: "Ah... Lol".""",
410208: """on the radio, science and technology news
радио: macaques recognize their kin in photographs.
я: and give them a plus""",
410209: """xxx:
Good evening, forum folks. There's a problem. Behind the wall a dog constantly barks (small by the voice). Barks daily, no less than 2 hours, 30 barks a minute. That's a total of 1.5 thousand barks a day while the owner is at work. Has anyone faced a similar problem and how did you solve it? Would like to hear opinions.""",
410210: """A friend's mom is a schoolteacher, her colleague, a top-category teacher, for fun sent a scan of her pay slip to the presidential administration, they accepted it, asked the education department what the hell. They checked everything and answered: you're just a bad teacher.
They're now wondering, if the whole school sends their pay slips — a bad school? a bad city?""",
410211: """xxx: my dad and I were shocked! standing in the kitchen, chatting. mom runs into the kitchen, pours some baking soda on the table, levels 2 lines, snorts them one after another — one nostril, then the other. calmly leaves, and dad and I keep standing silently with our mouths open.
xxx: poor man's high, damn!
ххх: later asked her what that was, turns out she read a new recipe for a runny nose in the newspaper :D""",
410212: """xxx: Don't want to go to Nazareth?
yyy: Nah. I only know one song of theirs.
xxx: So what, I don't know any at all.
yyy: Your loss. It's a good song.""",
410213: """ххх: I'm amazed at you, you've only worked at this organization for a week! What will the next record be?
ууу: Not my fault. When I came in they told me themselves that in their firm a lawyer must be honest with the client and give advice as clearly as possible.
ууу: Well when the client asked what he should do now, I, having looked at the documents, honestly and as clearly as possible answered: dry rusks and get warm clothes.....""",
410214: """xxx: Today I found signs of extremism in my phone.
xxx: It has a "regime change" function.""",
410215: """On Habr they discuss cleaning coolers from dust.

asv_nix
I personally use an enema and had the same at work, it really helps. There are also special enemas combined with a brush at the end for equipment, don't know what they're called.

osof
In unfamiliar company don't say you use an enema with a brush.""",
410216: """невролог: They say lightning doesn't strike the same funnel twice. Well, nothing of the sort!
невролог: Today a guy came to my appointment with a bandaged head, whom I treated all last spring for a concussion after an icicle fell. On March 21, exactly a year later, he went to nostalgically visit the place where he got injured. An icicle fell on him again. This time he got off easy, the chunk of ice grazed him. His head is almost unharmed, the partially torn-off ear was successfully sewn back on. Finally he asked whether to expect him next spring )))""",
410217: """anton: cards, checkers, roulette — which of these are gambling?
kwennar: none. cards are small cars with a "Druzhba" chainsaw engine, checkers are sabers, and roulette is a tool for measuring length.""",
410218: """Георгий: Just got money for a side job, gave the programmer his share, come home — the wife starts nagging from the door about some curtains for the kitchen and new wallpaper that absolutely must be hung before the mother-in-law's visit.
Георгий: Immediately a queue of thoughts appears
*back to shopping till night...*
*we just redid everything a year ago...*
*40 grand with change in my pocket*
*And programmer Lyokha is now drinking whiskey in a soft armchair*
*And a half-naked stripper is sitting on the arm of the armchair*
*And he did invite me to go with him*
the nameless and index fingers twitch stupidly in the air, and in my brain feverishly pounds *Ctrl+Z, you bitch, Ctrl+Z!!!1111*""",
410219: """A woman always remains a woman.
Today I watched a young lady, while reinstalling the OS (from Ubuntu to Win7, by the way), take a mirror out of her purse between tasks and fix her makeup.
And, sighing, added: "Looks like I won't get to grow a beard after all".""",
410220: """xxx: In the morning an SMS came from a friend. "Fuck!!! Bitch, give back my monitor! I'm so tired of sitting with 15 inches!!!"
xxx: To say I was stunned is to say nothing
xxx: Before I could write back, a second SMS comes from him "Chill! It was a dream...\"""",
410221: """xxx: so who works for you? What departments?
yyy: the director, an engineer, and a guy who doesn't know what his position is.""",
410222: """Laptop return request.
When I read it I decided to photocopy it and post it here. So:

Request.

Please consider my request to return the HP 625 Laptop bought by me on 15.03.2011 for 16200 rubles, for the reAson that I couldn't master working on it. A relative promised to help with mastering it, but showed me once and that's it.

23.03.2011 /Signature/""",
410223: """xxx: Glory to Wifi!
xxx: The kotte can't chew through it!""",
410224: """ххх: why hasn't the form on the site been changed yet?
ууу: zabli
ххх: ...I hope you meant the word "zabYli\"""",
410225: """xxx: listen, I just got so scared! I have a program that takes a photo, finds faces in it and makes portraits out of them
xxx: and then I see some totally unfamiliar mug. I know the photo shows me with my friend, but here's some guy's face, and it's all blurry and blue...
yyy: I'm already scared!
xxx: I'm writing and shaking myself! so I go look at this photo in full size, already picturing some blue guy behind us that can only be seen in the photograph, like in horror movies... and then I realize that this bastard (not the guy, the program, the snake, rather) found the guy's mug on a T-shirt (!!) hanging on the wall behind us and made a portrait out of it!
xxx: you could become a stutterer from that!""",
410226: """Ziggy: need to turn the porn up louder. otherwise the neighbors probably already think I have no personal life at all...""",
410227: """At the doctor's:

xxx: Is anything bothering you?
yyy: The draft office! :)""",
410228: """kora_f: When one stick and eight holes defeat a whole army...
don't you know this cartoon?

werewolchara: "The Gatling Gun"?""",
410229: """A friend and her husband took the cat to the vet clinic for surgery. Second time in a month. First time they were sewing the tail back on, now — sewing the cheek. The friend's husband asked the vet: Won't the cat's memory get worse from the anesthesia?
The vet promised that the cat won't be reading and writing anymore, but will find its way home.""",
410230: """Thugz: my pants can't hide my elevated mood""",
410231: """ya_love: Until 12 I dreamed of becoming a singer )
ya_love: I sing great, by the way, only my voice let me down, and my hearing too.""",
410232: """phone call:
- Lyokh, can you stop by the store and buy vodka and mineral water? Natakha will be making cocktails..""",
410233: """shilov: Zheka, the slogan needs to be from life, not pulled out of thin air. Otherwise we won't sell anything! Give me a slogan from life!
cmd: "Life is no picnic. (c) Lada KALINA!"
cmd: go, sell it. from life, fuck.""",
410234: """ххх: Well, if you meet your husband's mistress on the street, you wouldn't grab her hair, would you?
ууу: She won't get that. I'd stoop only to the phrase "Don't open your trap at someone else's dick\"""",
410235: """Lying with my wife in bed somehow. She asks me why I'm tossing and turning. Yeah, I say, the kotte settled at my feet, can't move him and he's in the way. Want me to move him? she asks. I nod. A second later there's a wild meow, a leg kick and a scream across half the apartment: "This is Sparta!\"""",
410236: """xxx:
Today I watched "Nu, pogodi" and you know, it turns out it's not just a cartoon about animals that look like people. It's a whole sci-fi. A post-apocalypse for humanity.

xxx:
It's amazing how they managed to cram such a universe into such a cartoon. It's all very simple. The thing is, BEFORE the animals, like the Wolf and the Hare, there were people in that world. You can see it in the museum episode: there's a caveman, there's human armor, there's Egyptian painting, there's even the Venus de Milo. Meaning, people really did live there once! But then upright animals came from somewhere and either enslaved us or simply destroyed us. All that's left of humanity is our culture, which these creatures adopted from us exactly, down to the alcoholics watching football in wife-beaters with fish in their hands.

xxx:
What's interesting, not all animals in the world of "Nu, pogodi" are upright and intelligent. There are horses there that are still used for riding, and there are even ordinary domestic cats. If you observe the whole series, the most frequently encountered animals will be Wolves, Hares, Hippos and Dogs. You also meet Lions and Bears. From this you can suppose that the Wolf chases the Hare not out of hunger, since they have a developed food industry adopted from people, but out of some ideological disagreements. It's not excluded that these are two hostile alien species, although Hares don't feel such hatred toward Wolves.""",
410237: """xxx: your equalizer config is called "the drummer got his timpani stolen\"""",
410238: """xxx: No, the biggest nerd is Seryoga. Did you know the wall clock above him hasn't worked for half a year?
ууу: wait, but it always shows more or less the right time!
ххх: EXACTLY! He's been adjusting the clock every fifteen minutes for half a year!!!""",
410239: """another call to tech support:
я> I have a static IP service connected, but it keeps changing
он> is your internet working right now?
я> yes
он> then I don't understand what your problem is""",
410240: """I work at Bely Veter at the register. Yesterday there was a funny case. A young guy comes to the register and has me ring up all four parts of WoW and a three-month time card.
Я: Are you sure about this?
Он: Yeah, it's not for me. For an acquaintance's birthday. He started hitting on my girlfriend.
Yeah, back when people just got their faces beaten for that, it was more humane.""",
410241: """Sanchous: and one more thing... This year we're going hiking without buckwheat!""",
410242: """/*Conversation about little doggies*/
Ххх: what do you feed your mouse?
Ууу: everything
Ууу: dog food, soup, porridge with meat, vegetables, soft toys, LEGO..""",
410243: """masha_miller: Classic Petersburg intelligent grannies in the metro with e-readers are incredibly delightful :)""",
410244: """3ABXO3:
So imagine: On the ferry to Helsinki we took 4 big bags. Decided to shove them into the luggage lockers. And one cell there costs 4 euros. Well, we're not taking two. Started shoving into one. And one asshole sailing with us turned on the Tetris music on his phone!
3ABXO3:
From the side it's a picture — two morons to the Tetris music shoving a pile of big bags into a small locker. The Finns drinking nearby even stopped drinking from laughing. One started filming. The funniest thing is we did cram the bags in!
Freem:
Tetris rules!""",
410245: """_Lanze_: When I was devouring sci-fi in the distant nineties, I could of course imagine that in early 2011 I could be struck by plasma.
_Lanze_: But I didn't think the cause of the wound would be a 42-inch plasma panel falling on my foot.""",
410246: """from a forum flea market:
xxx: need to make a 3D model of a military unit, urgently, who can help write me in PM.
yyy: Good afternoon! What program do you need the model in and how urgent?
zzz: You clearly didn't serve....=)""",
410247: """ххх: eh, whoever worked in rusupport doesn't laugh at the circus...
ууу: I didn't work in rusupport. I worked as a salesman. I don't laugh at the circus either B(
ххх: yeah, tough
ууу: The simplest thing, for example.
- Give me a hat for a girl.
- What size?
- Girl, come here!
And a mare of about 25 rolls up to the counter — that very "girl" x_X""",
410248: """A story from a theater choreographer. Olga (an intelligent and refined aesthete) climbs the stairs to her apartment. On the landing, as usual, the shirtless blue-collar neighbor is smoking. "Smoking topless again, Vasily Ivanych," she says reproachfully. The neighbor, blinking in bewilderment, answers — no, I'm smoking "Bond".""",
410249: """Sitting and solving calculus. Mom comes over and says the calculator isn't working.
Мама: I press 210-105 and the equals button and nothing happens.
I started examining it, flipping various switches I don't even know what they're for.)
The truth dawned only after about two minutes...""",
410250: """Ом
decided to shoot on film. now I'm like a person who just bought a camera — I photograph all kinds of ordinary crap, but with a special, "artistic!" eye

Ом
and if a cat gets in the frame, it automatically turns the photo into a masterpiece""",
410251: """xxx: It used to be: "Your wallet or your life!".
xxx: And now "Login or Register!\"""",
410252: """> Office observation:
> If a girl is sleepy in the morning — they'll ask her where she was hanging out all night.
> If a guy is sleepy in the morning — they'll ask how much he drank.

If a programmer, regardless of gender, is sleepy in the morning — no one will ask anything, it's all clear anyway.""",
410253: """коллега1: Do you know, colleagues, there's a theory of world order according to which if a tree falls somewhere in a deep forest and there's no one around, then there will be no sound from the falling tree either...
коллега2: Hmm, can you trick the sound somehow?
коллега1: What do you mean trick? Why?
коллега2: Well I mean make it so the sound thinks you're not nearby, but you're actually hiding somewhere close by and checking — will it be heard or not.""",
410254: """xxx: on our 4th floor the aunties and uncles were drinking and got into a fight, the aunties called the cops, all the uncles were taken away
xxx: now the aunties are lamenting to the whole entrance that the cops are bad, but without the uncles it's boring.""",
410255: """Лис:
a colleague guy, just on the phone with a supplier: "What exact parameters do you need? One second, let me check". Covers the receiver with his palm, looks around the office, scratches the back of his head and with the words "How the fuck should I know!" politely into the receiver: "Couldn't check"...""",
410256: """Новость: "Plutonium found in the soil on the territory of the Fukushima-1 NPP is not dangerous to health"
Комментарии:

xxx: Yeah we're dumb. Again didn't realize that plutonium comes in good democratic and bad totalitarian.
yyy: it's food-grade plutonium""",
410257: """In the words of an acquaintance:
Папа (an auto mechanic) comes home after a hard workday. At home his baby is crying its eyes out. No one can figure out what's wrong.
The baby's mom: Maybe something hurts him?
Папа: What could hurt him, everything on him is new?""",
410258: """comment on a site under a group-sex porn video:

САМВЕЛ 26.02.2011
They've got nothing to do, huh. They'd better clean the snow in my yard, you can't walk there""",
410259: """123: in dragon age 2 in the brothel I accidentally, without looking closely, fucked an elf. Second day thinking whether to tell the wife...""",
410260: """КРАЙС: If the kotte stopped eating aloe, it means the bastard found an old cutlet somewhere""",
410261: """xxx: All the switching lasted about 10 minutes.
xxx: What the fuck is the probability of randomly flipping channels and watching 10 minutes of broadcast without sound?
yyy: I once downloaded a movie with a normal-sounding title, clicked 8 different spots on the timeline — all fine.
yyy: Sat down to watch with my wife — turned out to be porn.""",
410262: """I wanted to tell about the relativity of perceiving avant-garde painting.
Today in our MHK class we were looking at a painting painted in the Rayonism movement. The teacher starts reasoning that, you know, many avant-gardists depicted animals: horses, dogs, but here a crow is depicted.
And under the painting it's signed in Spanish "Cats"...""",
410263: """Diamante: that's always how it is with us :(
Diamante: they switched to daylight saving time, but not the weather!""",
410264: """xxx: I'm in shock.
xxx: 2 first-line admins (read: any-key guys) walk around with traumatic pistols.
xxx: personally I'd be scared.
yyy: stern admins!
yyy: the accountants are probably in shock too
yyy: the moment a paperclip crunches in the printer, cold steel presses against the back of the head""",
410265: """JET: As a kid I was very afraid they'd make me play the piano. Because seeing people with guitars and violins in the metro I was 100% sure I'd have to drag a piano behind me. What piano, I could've gotten a grand piano! (""",
410266: """Viper: Looking at the current weather, one thought comes: This spring, winter turned out especially well.""",
410267: """XXX: A simple law of nature — a two-legged one always runs slowly, a four-legged one is always faster.
YYY: Right, that's why supersonic turtles chase ostriches and gnaw them to the bone on the run""",
410268: """ж: Pash, where's our cat? haven't you seen him?
м: he's in the bathtub.
ж: What's he doing there?
м: I pissed on him.
ж: WHAT?!
м: I pissed on him and locked him in the bathtub.
ж: Why??
м: how long can he mark our armchairs? now I've marked him.""",
410269: """xxx: need a golf ball, 2 pcs.
yyy: sir aristocrat?
ххх: ser is making a gear-shift knob for the kopeyka, I had one, very comfortable...""",
410270: """Оленяка: thinking I should delete my ex-boyfriends' phone numbers from my phone...
Оленяка: all after mom yelled from the kitchen "come quick, a dickhead's calling you\"""",
410272: """xxx: how's it going at the draft office?
ууу: fine. they remember, love and wait.""",
410273: """Shoei: 29.3.2011 Meeting of president D.A. Medvedev with students at MEI
ВалинаГ: cool
ВалинаГ: going?
Shoei: no. they're not letting anyone in )))""",
410274: """Tairo: Don't make my slippers laugh! They tickle my heels and I can't read the forum properly.""",
410275: """xxx: Bluetooth headphones have an interesting undocumented feature.
xxx: You only learn that a pickpocket pulled your phone when the bus pulls away from the stop...""",
410276: """xxx: Since this year my accountant, paying payroll taxes, whispers when sending the payment: "choke on it"
yyy: And my fool of one is happy. Says, maybe now we'll finally live normally, look how much tax we're paying. Damn optimist.""",
410277: """ххх: Bit my tongue in the morning, and now one bump there got inflamed! Hurrrts T_T
ууу: Well what did you expect, a dead pixel.""",
410278: """When I was 5, I went with dad to the play "Uncle Au". Dad got tickets in the first row, so we and the actors could see each other perfectly. Shortly before the play, dad the surgeon got into a fight at the hospital with a drunk pestering a nurse, and got a black eye. I bumped into a table corner and got a black eye under the same eye. It's worth noting that dad and I look a lot alike and it's extremely hard not to tell I'm his daughter.
Now imagine: a children's play, in the first row sit a big gloomy man with a black eye and a little gloomy girl who looks like him, also with a black eye.
Gods, those actors were openly giving us wary looks.""",
}


def main():
    inp = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_28.jsonl'
    outp = '/home/hatch/workspace/translation-work/bashim/out/chunk_28.jsonl'
    recs = [json.loads(l) for l in open(inp) if l.strip()]
    batch = recs[100:200]
    want = {r['id'] for r in batch}
    assert set(EN) == want, 'key mismatch: %s' % (set(EN) ^ want)
    with open(outp, 'a') as f:
        for r in batch:
            en = EN[r['id']]
            ru = r['text']
            assert en, r['id']
            assert en.count('\n') == ru.count('\n'), 'newline mismatch %s: en=%d ru=%d' % (r['id'], en.count('\n'), ru.count('\n'))
            f.write(json.dumps({'id': r['id'], 'ru': ru, 'en': en}, ensure_ascii=False) + '\n')
    print('batch28_2 OK, wrote', len(batch))


if __name__ == '__main__':
    main()
