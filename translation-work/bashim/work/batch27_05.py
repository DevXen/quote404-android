#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 5 (input lines 401-500). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409476: """Tarrasque:
Рыжик, did you get the "project" from the customer? They've been blowing up the phone for half an hour, saying they "sent it 3 hours ago".
Рыжий танк:
"They're shamelessly lying". They sent it 15 minutes ago, after I called them back. "file link"
Tarrasque:
The fuck is this shit?!
Рыжий танк:
It's 47 pages of handwritten scribbles, scanned in the best quality and stuffed into five-megabyte images each. All this crap would easily fit into a five-page Word file of like 10 kilobytes.
Tarrasque:
Hm. And the second archive?
Рыжий танк:
O-o-oh! That's another 18 pages of doodles called "drawings and diagrams". Also by hand. The topo map, road junctions and utility crossings were especially touching. My four-year-old niece draws better.""",
409477: """xxx: "Russia has acquired a yacht in Turkey worth 30 million euros, reports REGNUM. On it Russian president Dmitry Medvedev will receive heads of state and government - guests of the 2014 Sochi Olympics". (c)

xxx: The guys aren't even hiding it, they already know who'll be president.""",
409478: """xxx: Three out of every 10 Russians feel safe walking the streets of Russia at night.
xxx: yeah, but when those three walk the street, the remaining seven feel unsafe walking the streets of Russia at night...""",
409479: """1> Oh these non-humanities girls.
>me: You're my sunshine :)
>she: Do you also think that if I overcome laziness I'll be capable of a small thermonuclear fusion?
1> Gotta filter compliments apparently.""",
409480: """xxx: my brother reminds me of the terminator.
xxx: "I need your computer, internet and shorts" (""",
409481: """Гарфанг: I thought you were a busy person )
Гарфанг: and you're playing WoW )
Tranquillia: I'm going through a tough period in life, but I feel sorry for my liver!""",
409482: """Жена (10:58:32 26/01/2011)
sat down all the plush toys so he'd (the little one, soon to be 1 year old) know that there are many animals and they're all different. including a cow, a mouse, a lamb, and told about all the rest. I say give me the mouse. and he dashes to the computer desk, pulled himself up on tiptoes, got the mouse and is dragging it""",
409483: """killerloo> They can be dangerous. One such today in a restaurant came up to the table, took my pack of cigarettes, lit one, choked on the smoke, said "Idiot!", put out the cigarette in a glass of water.
A kind stranger woman, could've stabbed a fork in my back for instance ...""",
409484: """<xxx>: How was the trip?
<ууу>: It wasn't. Mud, you can say there's no road. Got all the cars stuck and looked for a tractor.
<xxx>: Nemarych said he flew through there in his Volga.
<zzz>: Yeah, they even made a movie about his Volga, "Black Lightning\"""",
409485: """xxx: and my neighbor turned on benny benassi - satisfaction and is drilling the walls with a hammer drill :D""",
409486: """Vuillaume: If you're in the Emerald City — get brains. Great stuff.
Gods_Tail: Brains are for the scarecrows.""",
409487: """k0ld: meet, keyboard, this is coffee
k0ld: coffee, meet, this is keyboard""",
409488: """ххх: I'm a cat. I walk by myself and my nerves aren't subject to spring.
ууу: got spayed?""",
409489: """<Metalclaw> our characters of classic literature are Pechorin, Onegin, Bazarov and Chatsky. meaning an introvert-schizoid, a rich emo, an angry nonconformist and a member of a destructive political movement.
<Metalclaw> and you ask why our youth is like this ;)""",
409490: """my uncle: hi, nephew.
my uncle: I've got a tragedy - my drivers crashed, can't play or listen to music
my uncle: asked online how to reinstall Windows through the BIOS, and they wrote me: take out the hard drive, open the cover and erase it with an eraser, then write "windows" with a black marker, put it back and go.
my uncle: bastards!""",
409491: """ххх: Why the hell did you buy a second computer? got money to burn?
ууу: I showed mom VK :`(""",
409492: """Client: My sound is gone, what can I check?
Admin: You can easily watch Charlie Chaplin movies or any other silent film.""",
409493: """xxx: we're lucky, the room has euro-windows. You take a shirt or say a t-shirt that urgently needs washing (the method is only effective in winter), clamp it between the mosquito net and the window for 15 minutes. Bam and the thing smells of freshness and alpine meadows =)
yyy: and you've only lived in the dorm for half a year...""",
409494: """ZO: daaamn
ZO: mom covered the pot with a plate
ZO: the plate fit perfectly
ZO: I FUCKING DON'T KNOW HOW TO GET IT OUT OF THERE WITHOUT FLIPPING THE POT!""",
409495: """If you carefully place something heavy on top of a sleeping True Cat Owner's blanket - you can watch the "process" of rolling from side to side in slow motion all night, and even sleepy attempts to scratch that heavy thing behind the ear ;p""",
409496: """Tornado201: By the way, what's your religion?
СуперОлег: peasant
Tornado201: Maybe Christian?
СуперОлег: no, peasant from the word cross, if you're such a moron you don't know that, that's your problem""",
409497: """Discussing rear-wheel drive in winter

Sobig: I once did a 360 and drove on with a satisfied look... that I didn't piss myself""",
409498: """Kolobok probably never thought that all possible emotions would be expressed with his help.""",
409499: """I just encroached on the sacred.

An electrician came to me, making excuses for yesterday's absence:
- Volodya, I got it, I'll fix it, it won't happen again.
- Got what?
- I should've called you in the morning right away to say I drank yesterday, can't come in.
- Or maybe you just shouldn't drink before work at all?

He looked at me with such sincere surprise, I was even taken aback.""",
409500: """xxx: someone wrote here that as soon as flying cars appear in Russia, air with potholes and ruts will appear right away.
xxx: he hasn't flown a helicopter over Siberia, oh he hasn't...""",
409501: """xxx: Legal forum. Question: what to do, a person took a loan, nothing to pay it back with, the penalty has piled up. 36 comments. Went in to read. Thought they'd explain to him that under article 333 they'll cut his interest amount. Turned out the whole world was teaching him how to sell a kidney more profitably...""",
409502: """xxx: recently saw D'Artagnan and the 3 Musketeers on TV
xxx: funny rewatching now - you notice lots of new stuff
xxx: there was a scene where the main guy, after chatting with Constance (the landlord's wife, btw), goes downstairs. the landlord shouts after him, so when are we paying for the apartment? D'Artagnan slams him against the wall, draws his sword and yells that he's got no money and if the fucking noble don hears about rent again, he'll smear him
xxx: I've been renting an apartment for years, never occurred to me you could do it that way""",
409503: """xxx: Yesterday there was a report about a dude who robbed drug dealers. At the end of the report it said something like: "If you also suffered from his criminal actions, call 7xxxxxx\"""",
409504: """xxx> Your dog looks semi-licensed )
yyy> semi-pedigree?
xxx> yeah, exactly""",
409505: """<Sgt. Joker> The State Duma passed the "On Police" law in the second and third readings at once, reports Interfax. All three opposition factions - CPRF, LDPR and "A Just Russia" - voted against the law, but 315 votes of "United Russia" were enough for the final adoption of the law.

<Sgt. Joker> But at least we have a multi-party system in our country!""",
409506: """xxx: I wonder, can he issue even one paper faster than in a month?
yyy: No. He's a professional. He took the Bureaucrat's oath.""",
409507: """_Lanze_: Found a very sysadmin-y last name - Spasibukhov =)""",
409508: """xxx: In the American movie "Independence Day" the president gets into a fighter jet and flies off to fight the invading aliens.
xxx: The Americans were joking, but I know one of ours who takes it at face value""",
409509: """Helen: Digging through the computer, I stumble on a file name clearly created by my sweetie: "me_with_katenka.jpg"
Helen: You're dead, I think, you lovey-dovey fucker...
Helen: I open it, and he's there at the Hermitage, by the bust of Catherine the Great""",
409510: """Once there was a conversation about dreams. I ask my web-designer friend:
me: and what do you dream about?
him: that only one browser existed in the world...""",
409511: """ххх: No, the cruelest trolling in my life was when me and some guy (don't even remember who anymore) about 15 years ago played Ninja Turtles 2 on the Dendy, and he told me that when in the first mission you kill Rocksteady and at the moment Shredder comes out of their drill-machine, you need to do a super hit over April, then the turtle will freeze, Shredder won't be able to capture April and you'll get straight to the Technodrome!
ххх: How many times I tried to reproduce it.... ALL THESE YEARS!!!!!!""",
409512: """xxx: Never before have I typed on a keyboard so carefully as when answering my wife's question "when are you heading home?" with the phrase "wait, we're about to start the gateway\"""",
409513: """Rio: why is your line spacing so big?
Rio: after my question I feel like Little Red Riding Hood""",
409514: """<Cam> We had it more fun. One detective went to an arrest in his own car. That'd be fine, if the dude didn't do car tuning in his free time.
<Cam> I think at the sight of the eye-gougingly red Oka with hubcaps, mudflaps, underglow and a white furry steering wheel, the criminal elements surrendered purely out of surprise.""",
409515: """mutorga: grandma's TV broke and she thinks I should fix it
mutorga: but I'm a programmer... (""",
409516: """BlessSin: yesterday the director told me: "Don't smile, I can't scold you when you're smiling" :-D""",
409517: """Q: do you have twitter?
Z: all I've got is a sweater and a printer.""",
409518: """xxx: from a fantasy point of view a vacuum cleaner is just a converter of lightning magic into air magic""",
409519: """YYY: How's work going by the way?
XXX: normal-ish, more or less, but still kinda not great
YYY: do I have to choose from 3 options?""",
409520: """Grandma: what do you want for dinner - pasta or soup?
Me: pasta
Grandma: how much pasta can you eat?! I already heated soup for you

I think that's exactly what democracy is like in our country.""",
409521: """Lida: It'd all be fine, but as a kid I had a plush toy.
Lida: For years we all thought it was a hare, until someone accidentally fell on it.
Lida: Turned out it's a talking one and at that... "Hi! I'm a bear!...\"""",
409522: """Ivor Horton - Visual C++ 2005 basic course, page 80
"The number of players on a football team is an integer, at least at the start of the game\"""",
409523: """xxx: our grannies argued in the yard for a long time that when you throw a trash bag down the garbage chute a stranger's bag from above might fly past and shower you with dust from the pipe...
xxx: since then, twice in a month I've caught the moment when I walk up to the garbage chute and some dude from the Nth floor loudly yells into the pipe "fire in the hole" and only then throws the bag xD""",
409524: """From twitter:
Telling a girl about the unrest in Egypt, the demonstrations, the internet shutdown across the country. The girl asks: Is this all because of that shark?""",
409525: """xxx: what does imposing mean?
xxx: 'cause I already agreed""",
409526: """xxx: at 3 PM the alarm went off at the security post, someone pressed the emergency call button.
xxx: they found a bag with a box at the facility.
xxx: the guys rolled up, turned out the box was WITH WHISKEY!
xxx: took it for examination.
xxx: we're still defusing it.""",
409527: """XXX: Today, cleaning the room, I moved the wardrobe and saw cockroaches there
XXX: Gathering a party against me...
XXX: Smashed them with a slipper
XXX: They clearly didn't expect the raid boss to attack first...""",
409528: """<Radioactive_Mage> My first night shift fell on the day of my date. Didn't want to upset my girlfriend.
<Radioactive_Mage> In principle I could've slipped away (second floor, under the window a roof of the boiler room or something), but I can't just do that! I must take revenge on the system and hooligan the discipline!
<Radioactive_Mage> Took spare clothes in a little bag. Left alone, changed, dressed the skeleton standing in the office in my clothes, sat it at the desk.
<Radioactive_Mage> Stuck a fortune-telling card in one hand, and put a tangerine on the other. And took off.
<Allgood> You should've glued horns on too
<Radioactive_Mage> Then it wouldn't have been convincing""",
409529: """xxx: Compared to the government's plans to improve the economy, my erotic fantasies are way more realistic.""",
409530: """xxx
Vov, get lost! I don't have a habit of puffing my lips and sticking out my butt in photos! I've just got this figure and I'm drunk in heels here!""",
409531: """xxx: as kids we played "dress the doll", now we play "dress the char in crit gear\"""",
409532: """mom: how's your beloved husband?
me: he's sitting eating herring, chasing it with seaweed and washing it down with strawberry juice.
mom: he really doesn't want to visit me tonight, huh?""",
409533: """xxx: Today after years of banging my fingers on the keyboard I tried writing with a real pen on real paper.
xxx: I learned with horror that I'd turned into a captcha generator...""",
409534: """From the women's forum:
xxx: Urgently need advice, what do I do.
I've got about 2 hours in reserve.
MY HUSBAND FLEW OFF ON A BUSINESS TRIP WITH A BAG OF UNIRONED LAUNDRY INSTEAD OF THE BAG WITH HIS STUFF!
He's landing at the airport in 2 hours, he'll be calling in, what should I tell him so he won't be too mad?
yyy: If you have time - iron it.""",
409535: """Lensky: read a review of the movie Season of the Witch on one site: "went to the Diablo II movie adaptation. Played through as a paladin\".""",
409536: """ххх: listen, lend me some money
ууу: much?
ххх: well, like a grand and a half
ууу: didn't you get paid yesterday? where'd it go?
ххх: well you see, I saw some wonderful red shoes on sale, bought them. Then realized I had nothing to wear them with. Had to buy a handbag and gloves.
ууу: got it. Now I guess you're short on "surviving till the advance"
ххх: how do I put this... screw the advance, I'm short for a scarf (""",
409537: """xxx: I'll shoot a 7-season series about growing plants, call it Sprout
xxx: for ratings""",
409538: """xxx: you talk kinda don't-give-a-fuckishly and without emotion
yyy: without smileys, you mean?""",
409539: """FiMa: bought motion sensors, the plan is after all this is over, as soon as you walk into the hallway or bathroom, the light automatically turns on
Flash: I'll give you a cat )""",
409540: """Today at the supermarket - a five-year-old kid is rushing to the self-opening doors with the photo sensor. His mom catches him.
The kid with indescribable panic in his voice:
- Quick, the portal is closing!!!""",
409541: """On one forum of dembels there's a discussion of who the draft board caught and how.
ххх: Two things gave me away: thirst for freebies and poor knowledge of the city's geography.
ххх: Read somewhere an ad about hiring a sysadmin. The conditions - gorgeous. The salary - amazing. The amount of work - minimal.
ххх: Went to the address given.
ххх: Turned out to be the draft board...""",
409542: """A friend is choosing a wedding dress from a catalog:

Ххх: I found myself a dress! Cute. Look at the "Lamia" model! Like it?
Ууу: you're going to walk down the aisle in a dress from the "Ghoul" collection?
Ххх: why ghoul?
Ууу: well, or "nightmare"
Ххх: eeeee
Ууу: didn't you read Greek mythology in school? Lamia, daughter of Belus and Libya, whom Hera out of jealousy turned into a monster, kidnaps and devours children, and also drinks the blood of young men.""",
409543: """xxx: My wife tried to convince me to quit smoking with math.
ууу: How's that?
ххх: First she calculated the money we'd save in a day without smoking, then a month, then a year and a lifetime.
ууу: And how many years did she allot you?""",
409544: """xxx: today is groundhog day )
yyy: we'll find that out tomorrow.""",
409545: """xxx: Mish, you don't even discuss with your girl which lube to get for the evening?
yyy: No
xxx: Man, you're a slippery type!""",
409546: """xxx: Did you know there are dual-core atoms?
xxx: I mean processors.""",
409547: """xxx: Egypt right now is the perfect place for people trying to beat internet addiction...
yyy: Yeah, also for fans of playing street quests.""",
409548: """xxx: Mom called, was broadcasting something about "hemoapathy". Daaamn... "HEMOAPATHY"!!! I didn't immediately get that she meant "homeopathy\".""",
409549: """xxx: I make websites. Cheap, fast, professional.
yyy: Cute?
xxx: Obviously cute! I'll stick kittens wherever you say!""",
409550: """a review of the nintendo wii on yandex market:

Drawbacks: Smashed the chandelier with the joystick.""",
409551: """Asking my brother to check my drawing. He went to art school.
Him: It's crooked here, fix it!
Me: it's not crooked at all!
Him: Don't argue! With my trained eye I see better!""",
409552: """From the comments on the news about the lost Russian satellite on Habr:

ххх: They overdid it a bit, first satellites were falling, now they're flying too far away, probably it was said from above that nothing should fall on the ground anymore )
ууу: They're zeroing in. Like in Angry Birds.""",
409553: """Даин: a dude just called
Даин: says, let me name you arguments to use our antivirus, and you try to find at least one not to use it
Даин: probably ranted for half an hour
Даин: I had one argument "we've got Ubuntu on all machines\"""",
409554: """xxx: My Egorka lunges with a growl at all moving objects (including hands and feet), sinks in for a bite, tearing the skin right through. To get this beast off a limb you need to pry its teeth open. And if you approach its cage, it starts thrashing in it with rage and lunging at the bars with gnashing teeth.
yyy: Why are you writing this? People who are just about to get a rabbit visit this forum. They'll get the wrong idea about these glorious little animals.""",
409555: """xxx: I want someone to burst into my life and change everything in it.
yyy: Get a dog. =)""",
409556: """<[Sky]powered> Good people, tell me pls who has the current Word of Warcraft in share? let me download pls

<[PEER]Dyxa> [Sky]powered: Don't need Excel of Warcraft?""",
409557: """xxx: And my girl, learning that a mouse lives in her room, stuck glass shards in front of its hole so the mouse would cut itself and die of blood loss.
xxx: I also suggested hanging a little noose of thread - in case the mouse hangs itself on the way out.""",
409558: """xxx: your potatoes got fried
yyy: but I'm boiling potatoes
xxx: that's exactly what I'm getting at""",
409559: """An employee, worried about the monitor's effect on health, talks to our admin.
Employee: Is it true you can't touch the monitor with your finger?
Admin: Yeees, it leaves grease spots on the monitor.""",
409560: """Sitting at home, wiping my electric guitar with vodka. Well it wipes off skin grease and dirt nicely, and doesn't damage the lacquer. Munching candy I stole from the kitchen along the way. Wiped the guitar, put it on the stand. Just turning to the computer, mom walks in.
A five-second silent scene. Me sitting at the table, on the table a bottle of vodka, a candy. Mom's phrase:
- Feeling blue, sonny?""",
409561: """BlackLynx: Yesterday I left my car at the subway. Going down, walking up to the turnstile. Out of habit I feel for the alarm fob in my pocket, press the button. Duh: what am I doing wrong?.. First thought: the battery died:)""",
409562: """ххх: And he's already growing up, what will I do when he comes asking - mom, where did I come from?
ууу: So he asks, what then? )
ххх: Well I can't just tell him right away - sonny, you see, your dad and I used to work at one big company, he was a lead programmer, and I was a designer. We worked a lot and hard, had to live in the office for days, and there was only one couch per floor, we fought epically over it, and then one day it just happened that I felt sorry for him and didn't chase him off to the armchair...""",
409563: """Смех Дождя: And then we went caroling. A dacha village, dark, quiet and deserted - it's scary for us three fragile, defenseless girls to walk alone at night. But what can you do - gotta carol.
Смех Дождя: So we put on quilted jackets (big warm ones), dressed up in tinsel over them, Natasha took an axe and we went.
Смех Дождя: though we didn't carol up anything...""",
409564: """xxx: Skype is threatening me
ххх: got an email with the subject "Wherever you go, Skype will always be with you"
ууу: Big Brother is watching you""",
409565: """xxx: The accountant called this morning. In a panic. Says come urgently, there's some crap, all work is at a standstill.
ххх: Came, turned off non-printing characters in Word, left.""",
409566: """xxx: guys! save my reputation! my son asked what camel cavalry is called?!""",
409567: """Enthusiastically digging through the computer, a dialogue with my wife from my side:
- Honey, bring me the adapters.
- Are they in the box or in the bag?
- The ones in the bag.
The wife brings them and laughs, I ask again puzzled, the dialogue as it was:
- Where are my these?
- Are they in the box or in the bag?
- Yes!
I'm still thinking, she understood and brought them!!!""",
409568: """xxx: honey, I specially stole a bottle of beer for you from the swingers party!
xxx: are you happy?""",
409569: """Лада калина: Hi
Лада калина: I'm Ksyusha.
Ли$: and do you, Ksyusha, break down often?""",
409570: """xxx: Had to get into such an accident. Was getting ready for work - called a taxi. While waiting, sitting in my jacket I got warm and fell asleep. Waking up to a call from work, where are you and what the hell? Turns out I'd napped for an hour. OK let's call a taxi again, I say, miss, I called a taxi an hour ago and accidentally fell asleep, give me another car, I'm late. OK, wait for the call. Not a minute passed, she calls back laughing into the phone. You've got the same car still waiting, come out. While the driver was waiting for you, he dozed off too, and we lost him.
xxx: So I rode with him. And now I'm in a cast. Fate. (""",
409571: """yyy: Aren't you taking on too much?
xxx: They pile it onto me.""",
409572: """<@Lurker> the grouping of Russian satellites in the Pacific is growing""",
409573: """After my sister started driving, I realized that "FUCK!" is much faster to say than "BRAKE!\"""",
409574: """yyy: I was getting two certificates, for weapons and for driving, I go to the psychiatrist, announce that there should be two certificates, she asks, why two? Couldn't hold back, confessed that I want to drive a tank and shoot at people. They gave me the certificates.""",
409575: """xxx: If there's office plankton, there must be office whales too.""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 401, 500  # 1-based line numbers
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
