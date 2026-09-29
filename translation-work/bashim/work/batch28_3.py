#!/usr/bin/env python3
"""Translate bashim chunk_28 records 201-300 (input lines 200-299). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
410279: """xxx: and if you wave a cross at a werewolf and it helps, will you believe in god?
yyy: first I'll believe in the werewolf""",
410280: """Matrim: by the way, I've been meaning to tell someone.
Matrim: My student told the girl I'm sleeping with that I'm 100% gay.""",
410281: """Nefarian: I live near the zoo. Woke up to thunder and flashes from behind the curtain, looked out the window
Nefarian: I wouldn't wish on my enemy, hungover, seeing against a stormy sky and a "monuments made to order" sign a camel shitting on the tram tracks""",
410282: """ххх: Too bad you can't put a condom on your heart.""",
410283: """Мамонт: Well he's cool now.
Alex: ??
Мамонт: You're not in the loop? He bought a totally wrecked, not running Lamborghini. Though money-wise it came out like two normal cars. But he's cool now. Twirls the Lambo keys in his hands and shows everyone the vehicle title slip. And walks on foot.
Мамонт: Came to work all happy today, skipping, face joyful, says "hurray I bought a headlight. the right one". Then started asking to borrow money, like he doesn't have enough to live on. They tell him: "but payday was just the day before yesterday". And he's like: "I'm telling you, I bought a headlight. The right one.\"""",
410284: """Юльчик: An electrician came today, his fly undone, I look at him, like I'm ashamed to say anything, but I want to. That's how it goes — HIS fly is undone, and I'M the one embarrassed...""",
410285: """ххх: there's a note on the fridge saying "chicken", but the fridge is as empty as always. what could that mean
ууу: the chicken ate everything. left its mark""",
410286: """Alexey: Curling was invented for those who ride down the lane together with the ball in bowling...""",
410287: """Just came across an interesting resume:
Natalya, 21 years old, male
...
WinRAR — confident user.""",
410288: """ххх: Ira, do you have an inner voice?
ууу: well yeah...
ххх: and what does it tell you most often?
ууу: Ira, keep quiet!""",
410289: """xxx: "Fuck me running" — that's how my cat wakes me up in the mornings""",
410290: """Just had a call, a girl asks:
Она: Can we go replug something in the server room?
Я: What are you going to replug?
Она: Don't know, that's why we're calling you to ask what we should replug in the server room.
Я (stunned): And, actually, why?
Она: Our printer prints slowly!""",
410291: """xxx: Yesterday watched "Three Days to Escape"
yyy: Is it about growing flowers?""",
410292: """xxx: Back when I came home from school, a note from mom often waited for me on the table: "Sasha, fry yourself some eggs".
xxx: It always scared me.""",
410293: """xxx: Damn, why can't I meet people?
yyy: Because as a kid they taught you not to talk to strangers...""",
410294: """ххх: I'm naked
ууу: I'm slowly peeling your skin off...""",
410295: """ааа: is that Strauss playing right now?
ббб: sounds like the intro to Star Wars, or something like that
ааа: reminds me of KVN for some reason
ббб: oh that Strauss! stole from everyone!""",
410296: """xxx: Occupational safety specialists issued a $14k fine during an inspection of Flint's filming pavilions, where at least three actors didn't have condoms.
yyy: And I'd chew them out at a union meeting too! What the fuck is this? Tomorrow he'll climb to a height without a harness?""",
410297: """xxx: Hi! How are you?
yyy: fine, just my stomach hurts =(
xxx: maybe abs? ;)
yyy: yeah, they're cutting...""",
410298: """xxx: listen
xxx: I'm paranoid. When I boil pelmeni, it seems like one or two pelmeni disappear from the pot.
yyy: dude, I live in a dorm. When I boil pelmeni, half disappears naturally :)""",
410299: """Лира: On four legs, but not a dog. With a tail, but not a dog. Barks, but not a dog. Man's friend, but not a dog. What is it?
borg: a child?
borg: a wife?
borg: a job?
Лира: Don't know, Yana [8 years old] was asked to solve it in the world-around-us class.
Лира: The mechanic says — a car.""",
410300: """lizi: This is fucked. My grandma is mentally ready, and would even be glad if it suddenly turned out right now that I'm pregnant. Like kids, cool and all. But if she suddenly finds out I'm not a virgin, and, excuse me, fucking someone, that'll be the end... How do I go on living?""",
410301: """inkvizitor68sl: > That's plasma falling, and unity is carefully lying down.
inkvizitor68sl: Lying down carefully, slowly taking off its core and moaning invitingly into the syslog.""",
410302: """Two students in the cafeteria:
ххх: why is the line so small?
ууу: you were hoping to be half a class late too?""",
410303: """Mail.ru Q&A:
Question: What's best to wear a black checkered arafatka with?
Answer: With a Kalashnikov. Both fashionable and stylish. And most importantly — relevant for the moment.""",
410304: """Snow Green: my neighbor has two books on her shelf
Snow Green: "Japanese in Three Months" and "Assembler: It's Simple"
Snow Green: from the same series apparently""",
410305: """In the topic they're discussing gear.
ххх: Colleague, I'm an experienced tourist, I served in the army!
ууу: The tour operator really fucked you over with this trip, tourist.""",
410306: """xxx: I have a dilemma, the tablet is safer with a case, but even though the case is thin, the tablet gets thicker.
ууу: carry the case without the tablet: both thinner and safer""",
410307: """"The RF government approved the subsistence minimum for the country as a whole for Q4 2010 at 5 thousand 902 rubles per capita. The corresponding decree was signed by RF prime minister Vladimir Putin."

Comment:
xxx: Okay, the cats are covered, but when will they approve a subsistence minimum for people?""",
410308: """музофоб: Sitting here, chewing on sushki, piled a bunch in front of me, more in the nightstand...
музофоб: And the term "uninterruptible power supply" takes on a whole new meaning.""",
410309: """Standing at a newspaper kiosk, choosing a magazine, and I hear happy squeals. I turn around — two 26-year-old girls running toward each other shouting:
- God, how glad I am to see you! We finally met!
- Yeah!! I was just thinking of you yesterday! How I love you!
- And I adore you! Sorry, gotta run, let's call! Just don't forget.
- Of course, dear! Bye!
One of the girls headed to the metro, and the one who stayed walks up to the kiosk annoyed and quietly says — "Fuck! Of all people to run into!".
Yeah, those are sincere feelings.""",
410310: """xxx: All parents of toddlers are ninjas! Who else could so virtuosically catch a falling child? Stick out a hand a millisecond before the fall.
xxx: And all toddlers are ninjas. They fall exactly when the parent's hands are busy.""",
410311: """xxx: Came to work today in a new blouse, with a big fluffy fur collar — gray-white, long pile, plain faux fur. A colleague spent a long time figuring out what animal it's from, not believing it's faux, but for some reason got offended at the remark that it's a Chinese underbush long-haired shushpanchik...""",
410312: """from the discussion of Russia's draw with Qatar

xxx: [Мат запрещен, читайте правила!]ец [Мат запрещен, читайте правила!]ец [Мат запрещен, читайте правила!]ец [Соблюдайте правила] [Соблюдайте правила] [Соблюдайте правила]асы [Соблюдайте правила] [Соблюдайте правила ][Соблюдайте правила] [Соблюдайте правила]вшие!!!""",
410313: """From a conversation with a girl:

Она: Damn, 2012 is almost here, and I still have so many unworn clothes...""",
410314: """xxx: I'll pass my exams in Iben and on the holidays! until I find a job )
yyy: the student month of Iben comes together with the exam session...""",
410315: """I work at a shooting range, a guy (П) and a girl (Д) come in
П: we shoot for a wish
Д: if I win you give me a gold ring
П: okay
Done shooting.
The guy won on points.
Д: (contemptuously) CHEAPSKATE!!!""",
410316: """Ingoldo: About the culturedness of Kupchino residents, literally, legends circulate
izm: literally circulating with my mobile.""",
410317: """xxx: And my mom doesn't believe in the end of the world.
xxx: She says — whoever survived perestroika isn't afraid of ANYTHING anymore.""",
410318: """xxx: a girl at our place went on maternity leave
xxx: just stopped by their office, on her desk there's a sign
xxx: "Workplace reserved, please do not loot the equipment" and the boss's signature.""",
410319: """xxx: I have a question for you as a lawyer. If my husband and I forgot to turn off the webcam, does that count as producing and distributing pornography?""",
410320: """aqoustic: and his porn folder is called "fapka\"""",
410321: """DoZa:
and at my uni they refused to certify a (REAL!!!) certificate, arguing that I bought it (it's from a private clinic) and they only certify certificates from district clinics!

In the end I have to buy a fake certificate to cover a real illness. Madhouse.""",
410322: """xxx: usually boys give their dick a manly name, but this fucker
xxx: "don't let my server go down!\"""",
410323: """snorapp:
Come on! She's a boy, he's a girl — a normal heterosexual family.""",
410324: """xxx: Harsh times
xxx: I work at a hospital, went into the staff bathroom, instead of paper there's a roll of bandage hanging""",
410325: """Виталик: know what a fail is?
Виталик: it's when you went to get a haircut during work hours
Виталик: and the CEO is getting a haircut in the next chair""",
410326: """<xxx> an acquaintance is driving to the crossing by the tunnel, and there's a puddle
<xxx> driving through it, and there's a POTHOLE
<xxx> fell into it, drove out — and the license plate is gone — torn off
<xxx> climbed into the puddle for the plate, found 11 plates
<xxx> except his own""",
410327: """xxx: Just got an email from one of our clients saying his email isn't working.""",
410328: """Discussion of packs of stray dogs in the city.
lisi4ka: Raised a hand against a dog, where's the guarantee it won't kill a person later! Where does so much cruelty in people come from !???!
Fraggle: I ate a cutlet made of a cow for breakfast — where's the guarantee I won't snack on human for dinner?
таракан: My neighbor poisoned the cockroaches at home. Where's the guarantee he won't poison me tomorrow?""",
410329: """From a forum:
Gentlemen, please tell me where to find the recycle bin of the previous Windows 7? I have a ton of important stuff there...""",
410330: """xxx: I love gin and tonic with chewy chemical candies like Starburst. Yesterday I took two 0.5 cans and four packs of candy.
yyy: Did you puke rainbows?""",
410331: """brend1999: ...And sections vary, like an acquaintance's son came home from school and proudly announced he signed up for a cutting-and-sewing club. The marshrutka-driver dad nearly choked on his Baltika.""",
410332: """xxx: so how's tanya?
yyy: well she dumped me
yyy: says, you quit smoking, quit drinking and you'll quit me soon
yyy: decided to do it first""",
410333: """xxx: "Medvedev ordered all ministers to master blogs and social networks"
xxx: and also to shave beards and wear German dress.""",
410334: """Илья: gradually, from book to book it tells how the author trained with one Indian
Илья: trained to be a warrior of light
sa6adanilov: an electrician or what?""",
410335: """xxx: when I was replugging all the new little buttons, a strange feeling appeared
xxx: similar to when the bubble wrap runs out""",
410336: """xxx:
Hi. Seryozh can someone else's website be deleted from the internet?

xxx:
It's just text (a paper written). It's just that our teacher will check our papers for plagiarism. I don't have time to write a paper, I want to download someone else's from the internet.

xxx:
And delete the site I download from""",
410337: """ххх: Yeah, when I last made a smart face, I got a pass in accounting...""",
410338: """Lady_Bugaga [i] [18/03/11 02:11]
DIY
I propose adding a forum where you can learn to do everything with your own hands, the range of topics is wide, from handicrafts with temari and kusudama to homebuilding, all the way to creating a UFO with your own tentacles.""",
410339: """Sovushka (01апр 14:43)>
if during falling snowflakes, being outside, you look up above you, you can catch the feeling of flying through the boundless expanses of the Universe.
Sovushka (01апр 14:44)>
Until a snowflake gets in your eye.""",
410340: """A TV ad. On the edge of consciousness I catch the slogan — "Such-and-such diapers absorb up to twelve hours".
Before my eyes appears a Dali-style surreal — a diaper absorbing clocks: alarm clocks, wristwatches, cuckoo clocks...""",
410341: """xxx: ahead of me a tub was driving, on a bump a wrench fell out of it )
xxx: from the suspension somewhere )
xxx: brand new, shiny
yyy: means the mechanic himself is still holding on""",
410342: """Она: You! Scoundrel! You dragged me into bed just to sleep!""",
410343: """ххх: your problems are all bullshit.
ххх: my girlfriend, during my brief absence from the city, met my ex.
ххх: and slept with her.
ххх: everything's fine.""",
410344: """xxx: Hey, how's the spirit?
yyy: Mk. 14:38
xxx: What?
yyy: The spirit is willing, but the flesh is weak""",
410345: """ArhiDavid: after the first driving lesson I realized I need a car with an automatic
ArhiDavid: after the second, with cruise control
ArhiDavid: after the third, with a driver""",
410346: """Today in the store I watched an epic scene. Ahead of me in line are three: a sturdy guy in a leather jacket, a bum-looking little man smelling of booze and a seventeen-year-old kid. The guy in the leather jacket hands over a five-thousand bill and asks for a cola. Then the little man digs out crumpled dirty change and asks for vodka. The kid points at the beer shelves and says:
- Give me two bee... ahem — glances at the previous customers and thoughtfully — Give me a cola.""",
410347: """xxx: And at one clinic the reception had an announcement: "For registration you need: passport, insurance and a home phone". Below in parentheses crookedly added "just the number" — I wonder how many telephone sets they got brought in before that note...""",
410348: """xxx: Actually my parents wanted to name me Sasha. But when I was born, they looked and named me Vasya.""",
410349: """I love starting this life with a clean sheet, but someday this roll will run out.""",
410350: """Мася: You're wrong to think Anton didn't like us! We had such a nice chat... He even promised to buy us tickets to some Nirvana concert!!!!""",
410351: """ВиО
xxx:
How to die with dignity?

yyy:
Easy.

1. Grasp your dignity with your hand.
2. Die.""",
410352: """xxx: A flash mob is separated from fuckwittery by a nuu very thin line.""",
410353: """mishko: 3 socks lie by the bed in the morning. I think — so there's one more under the bed, I look — there are two more!""",
410354: """Che: but really, why are you in such a hurry to ask her out? )
xxx: well I'm just tired of beating around the bush, so she has no reason to think I'm just joking with her
Che: and you chose April 1st to ask her out.
Che: brilliant.""",
410355: """Она: this isn't a job, it's a madhouse
Он: :(
Она: You're not interested? :(
Он: listen, at the institute I did my internship at a psych ward
Он: Then I served in the army
Он: and now I work at Sberbank...
Он: Do you think you can surprise me with anything?""",
410356: """From Habr:
FaNtAsY
Yeah. And let's only use stationary phones. They don't break from a normal fall either. :)
ScayTrase
is a stationary one a payphone?
FaNtAsY
I meant a wired home one. But a payphone is COOLER! :)
Static
Of course it's cooler, try dropping it)""",
410357: """xxx: how did the partying go? what was interesting? )
yyy: A sea of alcohol, tasted a bunch of different cocktails yesterday
xxx: So what cocktails would you recommend now? )
yyy: Non-alcoholic ones""",
410358: """<gmb> am I the only one who thinks the term "tagged VLAN" is some kind of swear word, not a port setting? like your server goes down, and you, as an intelligent person, instead of "fuck me" exclaim "tagged VLAN"!""",
410359: """kate: My teacher told us that when he himself studied at the uni, they had a deaf old man teaching electrical engineering basics. Completely deaf. He even had a special device, hearing-improving, and it lay on the desk. The kind students, when the teacher turned away, stuffed matches into that device... But the point is that this deaf old man read everything off lips. And kept iron discipline. But students are inventive: a guy walks into the exam, loudly reads out the ticket, his friends hear it, come as close to the door as possible and shout him the answer. That's how they passed. But the old man was no slouch either. He figured out that students were standing behind the door shouting answers. And quietly walked up to the door and, after waiting a bit, kicked the door open with a strong kick...
kate: Teachers used to be harsh...""",
410360: """xxx: Paleontology news: "Dinosaurs warmed their eggs in geothermal springs"
xxx: I have unscientific thoughts )""",
410361: """KENb: They piss, the bitches, into the transformer, and then they're surprised... if they manage to be.""",
410362: """In the game World of Tanks:
The battle starts, the team has several American tanks (5-6).
One writes to the team: Yankees, there's oil on the enemy base, we need it!!!""",
410363: """ххх: I got pregnant even
ууу: From whom?
ххх: From fear""",
410364: """A friend, an MAI student, told me today.
His department was tasked with digitizing old blueprints. Well tasked and tasked, the guys did it with thoughts of production optimization and switching to CNC machines. And everything would've been fine, except when the friend asked where to send the work, they told him to the ARCHIVE. And to the question "what the fuck", they answered "We got the task, you completed it, and we'll keep making parts from paper blueprints as before..."

But our president is advanced, he learns everything from the internet.""",
410365: """ххх: selling a Makarov MP 654K pneumatic pistol, price 4500 rub.
ууу: in stores one like that costs 4k. is your markup for the frags racked up? )""",
410366: """Mad:
On a brick fence near work there's graffiti:
"Punks hoy, punks are alive, punks don't give a fuck about You".
That capital-Y "You" moves me to tears.""",
410367: """xxx: Rewatched 90s ads "Imperial Bank".
xxx: A one-minute ad has more meaning, acting and directing work than a modern multi-million "blockbuster"...""",
410368: """About affairs in Libya, discussion:
...
- People like you just don't understand democracy!
- What's there to understand — it's already at 118 a barrel today...""",
410369: """Дядя_Саша: Amurchek, how's it going?
Ваша_Амурчек: no swearing allowed here, so everything's fine""",
410370: """xxx: Today I saw a scene on the street
xxx: A guy was running away from a girl, she caught him and hit him, after which he doubled over
xxx: She started taking care of him and checking if she caused any injury
xxx: Well I laughed out loud
xxx: And then she stared at me...
xxx: Ran for about 5 minutes (""",
410371: """ххх: just don't understand why Star Factory graduates are called "graduates"?
ууу: what don't you like?
ххх: they're PRODUCTS!""",
410372: """<Pandafree> How to switch to zfs without formatting the partition, are there utilities?
<drookie> how to switch from Windows to Linux without reinstalling the system
<drookie> how to change the processor to x86 without turning off the computer
<drookie> answers to these and other questions in the fresh issue of the weekly "fuck if I know\"""",
410373: """<Maxxx> that's it, I'm fed up (
<no-man> what? she didn't come?
<Maxxx> no...
<Maxxx> I'm sitting in this damn Paris cafe right now, she hasn't even sent an SMS and isn't answering calls
<Maxxx> I'm already ordering my third baguette
<Maxxx> it's also freezing cold here, and I want a drink
<no-man> well, Maxik, WHAT can I advise you?
<Maxxx> NO!!! NOT THAT!!!!!
<no-man> well then eat some more of those soft French rolls, and do drink some tea! )""",
410374: """xxx. and today I masterfully gave the cat a shot. apparently not only a programmer but also a vet died in me.
yyy. if after your shot the cat in the cat doesn't die, then apparently — yes!""",
410375: """xxx: No idea why, but I registered on a dating site.
xxx: Soon I'll be professionally versed in carpets.""",
410376: """I come home, dad goes: "Why did you leave after the second class today?". I say: "How did you find out?" He: "I was watching the news at 12:00, there was a live broadcast from Novokuznetskaya, I saw you in the background going into the metro!". Fail.""",
410377: """Lying in bed with my girlfriend:
Я: Darling, shall we play doctor?
Она: Mmm... Let's... I'll be your nurse!
Я: It's lupus! You're all idiots!""",
410378: """Our fridge broke. While the new one arrived, the groceries (including raw chicken) stood on the windowsill in the warmth for two days. Today I call my wife. Dialogue:
Я: Darling, what are you cooking for lunch today?
Она: Chicken in the oven.
Я: But it stood at room temperature for two days, it probably went bad.
Она: I'll marinate it, fry it on the grill, it'll turn out to be an ordinary STORE-BOUGHT grilled chicken.""",
}


def main():
    inp = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_28.jsonl'
    outp = '/home/hatch/workspace/translation-work/bashim/out/chunk_28.jsonl'
    recs = [json.loads(l) for l in open(inp) if l.strip()]
    batch = recs[200:300]
    want = {r['id'] for r in batch}
    assert set(EN) == want, 'key mismatch: %s' % (set(EN) ^ want)
    with open(outp, 'a') as f:
        for r in batch:
            en = EN[r['id']]
            ru = r['text']
            assert en, r['id']
            assert en.count('\n') == ru.count('\n'), 'newline mismatch %s: en=%d ru=%d' % (r['id'], en.count('\n'), ru.count('\n'))
            f.write(json.dumps({'id': r['id'], 'ru': ru, 'en': en}, ensure_ascii=False) + '\n')
    print('batch28_3 OK, wrote', len(batch))


if __name__ == '__main__':
    main()
